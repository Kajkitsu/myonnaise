@file:Suppress("MagicNumber", "TooManyFunctions")

package com.ncorti.myonnaise

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.BehaviorSubject
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.concurrent.TimeUnit

enum class MyoStatus {
    CONNECTED, CONNECTING, READY, DISCONNECTED
}

enum class MyoControlStatus {
    STREAMING, NOT_STREAMING
}

/**
 * Class that represents a Myo Armband.
 * Use this class to connecting to it, send commands, start and stop streaming.
 *
 * @param device The [BluetoothDevice] that is backing this Myo.
 */
class Myo(private val device: BluetoothDevice) : MyoEmg() {

    /** The Device Name of this Myo */
    val name: String
        get() = device.name

    /** The Device Address of this Myo */
    val address: String
        get() = device.address

    /** The EMG Streaming frequency. 0 to reset to the [MYO_MAX_FREQUENCY]. Allowed values [0, MYO_MAX_FREQUENCY] */
    override var frequency: Int = 0
        set(value) {
            field = if (value >= MYO_MAX_FREQUENCY) 0 else value
        }

    /**
     * Keep alive flag. If set to true, the library will send a [CommandList.unSleep] command
     * to the device every [KEEP_ALIVE_INTERVAL_MS] ms.
     */
    var keepAlive = true
    private var lastKeepAlive = 0L

    // Subjects for publishing outside Connection Status, Control Status and the Data (Float Arrays).
    internal val connectionStatusSubject: BehaviorSubject<MyoStatus> =
            BehaviorSubject.createDefault(MyoStatus.DISCONNECTED)
    internal val controlStatusSubject: BehaviorSubject<MyoControlStatus> =
            BehaviorSubject.createDefault(MyoControlStatus.NOT_STREAMING)

//    internal val dataImuOrientationProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()
//    internal val dataImuAccelerometerProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()
    internal val dataImuProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()

    internal var gatt: BluetoothGatt? = null

    private var byteReaderImu = ByteReader()

    private var serviceControl: BluetoothGattService? = null
    internal var characteristicCommand: BluetoothGattCharacteristic? = null
    private var characteristicInfo: BluetoothGattCharacteristic? = null

    private var serviceImu: BluetoothGattService? = null
    private var characteristicImu0: BluetoothGattCharacteristic? = null



    // We are using two queues for writing and reading characteristics/descriptors.
    // Please note that we must always give precedence to the write.
    internal val writeQueue: LinkedList<BluetoothGattDescriptor> = LinkedList()
    private val readQueue: LinkedList<BluetoothGattCharacteristic> = LinkedList()

    /**
     * Use this method to connect to the device. You need to connect before start streaming
     * @param context A valid application context.
     */
    fun connect(context: Context) {
        connectionStatusSubject.onNext(MyoStatus.CONNECTING)
        gatt = device.connectGatt(context, false, this)
    }

    /**
     * Use this method to disconnect from the device. This will release all the resources.
     * Don't forget to disconnect to the device when you're done (you will drain battery otherwise).
     */
    fun disconnect() {
        gatt?.close()
        controlStatusSubject.onNext(MyoControlStatus.NOT_STREAMING)
        connectionStatusSubject.onNext(MyoStatus.DISCONNECTED)
    }

    /**
     * @return true if this object is connected to a device
     */
    fun isConnected() =
            connectionStatusSubject.value == MyoStatus.CONNECTED ||
                    connectionStatusSubject.value == MyoStatus.READY

    /**
     * @return true if the device is currently streaming
     */
    fun isStreaming() = controlStatusSubject.value == MyoControlStatus.STREAMING

    /**
     * Get an observable where you can check the current device status.
     * Register to this Observable to be notified when the device is Connected/Disconnected.
     */
    fun statusObservable(): Observable<MyoStatus> = connectionStatusSubject

    /**
     * Get an observable where you can check the current streaming status.
     * Register to this Observable to be notified when the device is Streaming/Not Streaming.
     */
    fun controlObservable(): Observable<MyoControlStatus> = controlStatusSubject












    fun dataFlowableImuGyro(): Flowable<FloatArray> {
        return if (frequency == 0) {
            dataImuProcessor
        } else {
            dataImuProcessor.sample((1000 / frequency).toLong(), TimeUnit.MILLISECONDS)
        }
    }





    /**
     * Send a [Command] to the device. Before calling this please make sure the device is connected.
     */


    @Suppress("NestedBlockDepth", "ComplexMethod")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Log.d(TAG, "onServicesDiscovered received: $status")

        if (status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        // Find GATT Service EMG
        FindGattServiceEmg(gatt)


        // Find GATT Service IMU
        FindGattServiceImu(gatt)


        // Find GATT Service Control
        FindGattServiceServiceControl(gatt)
    }

    private fun FindGattServiceServiceControl(gatt: BluetoothGatt) {
        serviceControl = gatt.getService(SERVICE_CONTROL_ID)
        serviceControl?.apply {
            characteristicInfo = this.getCharacteristic(CHAR_INFO_ID)
            characteristicInfo?.apply {
                // if there is only 1 item in the queue, then read it.  If more than 1, we handle asynchronously in the
                // callback. GIVE PRECEDENCE to descriptor writes. They must all finish first.
                readQueue.add(this)
                if (readQueue.size == 1 && writeQueue.size == 0) {
                    gatt.readCharacteristic(this)
                }
            }
            characteristicCommand = this.getCharacteristic(CHAR_COMMAND_ID)
            characteristicCommand?.apply {
                lastKeepAlive = System.currentTimeMillis()
                sendCommand(CommandList.sleepMode(SleppMode.NEVER))
                // We send the ready event as soon as the characteristicCommand is ready.
                connectionStatusSubject.onNext(MyoStatus.READY)
            }
        }
    }

    private fun FindGattServiceImu(gatt: BluetoothGatt) {
        serviceImu = gatt.getService(SERVICE_IMU_DATA_ID)
        serviceImu?.apply {
            characteristicImu0 = serviceImu?.getCharacteristic(CHAR_IMU_DATA_ID)

            val imuCharacteristics = listOf(
                    characteristicImu0
            )

            imuCharacteristics.forEach { imuCharacteristic ->
                imuCharacteristic?.apply {
                    if (gatt.setCharacteristicNotification(imuCharacteristic, true)) {
                        val descriptor = imuCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG)
                        descriptor?.apply {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            writeDescriptor(gatt, descriptor)
                        }
                    }
                }
                Log.d(TAG, "imuCharacteristic?.apply")
            }


        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        readQueue.remove()
        Log.d(TAG, "onCharacteristicRead status: $status ${characteristic.uuid}")

        if (CHAR_INFO_ID == characteristic.uuid) {
            // Myo Device Information
            val data = characteristic.value
            if (data != null && data.isNotEmpty()) {
                val byteReader = ByteReader()
                byteReader.byteData = data
                // TODO We might expose these to the public
                val callbackMsg =
                        String.format(
                                "Serial Number     : %02x:%02x:%02x:%02x:%02x:%02x",
                                byteReader.byte, byteReader.byte, byteReader.byte,
                                byteReader.byte, byteReader.byte, byteReader.byte
                        ) +
                                '\n'.toString() + String.format("Unlock            : %d", byteReader.short) +
                                '\n'.toString() + String.format(
                                "Classifier builtin:%d active:%d (have:%d)",
                                byteReader.byte, byteReader.byte, byteReader.byte
                        ) +
                                '\n'.toString() + String.format("Stream Type       : %d", byteReader.byte)
                Log.d(TAG, "MYO info string: $callbackMsg")
            }
        }

        if (readQueue.size > 0)
            gatt.readCharacteristic(readQueue.element())
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)

        if (characteristic.uuid.toString().endsWith(CHAR_EMG_POSTFIX)) {
            putEmgDataToDataProcessor(characteristic)
        }


        if (characteristic.uuid.toString() == CHAR_IMU_DATA_ID.toString()) {
            putImuDataToDataProcessor(characteristic)
        }

        // Finally check if keep alive makes sense.
        val currentTimeMillis = System.currentTimeMillis()
        if (keepAlive && currentTimeMillis > lastKeepAlive + KEEP_ALIVE_INTERVAL_MS) {
            lastKeepAlive = currentTimeMillis
            sendCommand(CommandList.sleepMode(SleppMode.NEVER))
        }
    }

    private fun putImuDataToDataProcessor(characteristic: BluetoothGattCharacteristic) {
        val imuData = characteristic.value
        byteReaderImu.byteData = imuData
        Log.d(TAG, "imuData.size) "+imuData.size)

//        var list = ""
//        for(i in 0 until imuData.size/2){
//            list= (+(imuData[i])).toString()
//            if(i%4==0) list+=" "
//        }
//        Log.d(TAG, "imuData. 0<->size/2) "+list)
//        list = ""
//        for(i in imuData.size/2 until imuData.size){
//            list= (+(imuData[i])).toString()
//            if(i%4==0) list+=" "
//        }
//        Log.d(TAG, "imuData. size/2<->size) "+list)

        dataImuProcessor.onNext(byteReaderImu.getBytes(10))
        dataImuProcessor.onNext(byteReaderImu.getBytes(10))
        //   dataImuProcessor.onNext(byteReaderImu.getBytes(10))

        //            for (x in 0..2){
        //                dataImuAccelerometerProcessor.onNext(byteReaderEmg.getBytes(1))
        //            }
        //            for (x in 0..2){
        //                dataImuProcessor.onNext(byteReaderEmg.getBytes(1))
        //            }
        //  Log.d(TAG, "byteReaderImu = imuData "+imuData.size)
    }

    override fun writeDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor) {
        writeQueue.add(descriptor)
        // When writing, if the queue is empty, write immediately.
        if (writeQueue.size == 1) {
            gatt.writeDescriptor(descriptor)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        Log.d(TAG, "onDescriptorWrite status: $status")
        writeQueue.remove()
        // if there is more to write, do it!
        if (writeQueue.size > 0)
            gatt.writeDescriptor(writeQueue.element())
        else if (readQueue.size > 0)
            gatt.readCharacteristic(readQueue.element())
    }

    fun sendCommand(command: Command): Boolean {
        characteristicCommand?.apply {
            this.value = command
            if (this.properties == BluetoothGattCharacteristic.PROPERTY_WRITE) {
                if (command.isStartStreamingCommand()) {
                    controlStatusSubject.onNext(MyoControlStatus.STREAMING)
                } else if (command.isStopStreamingCommand()) {
                    controlStatusSubject.onNext(MyoControlStatus.NOT_STREAMING)
                }
                gatt?.writeCharacteristic(this)
                return true
            }
        }
        return false
    }


    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange: $status -> $newState")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "Bluetooth Connected")
            connectionStatusSubject.onNext(MyoStatus.CONNECTED)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // Calling disconnect() here will cause to release the GATT resources.
            disconnect()
            Log.d(TAG, "Bluetooth Disconnected")
        }
    }


}