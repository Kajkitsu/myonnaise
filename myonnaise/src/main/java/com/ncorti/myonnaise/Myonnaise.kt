package com.ncorti.myonnaise

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import io.reactivex.*
import java.util.concurrent.TimeUnit

class Myonnaise(val context: Context) {

    private val blManager = context.getSystemService(Activity.BLUETOOTH_SERVICE) as BluetoothManager
    private val blAdapter = blManager.adapter
    private val blLowEnergyScanner = blAdapter.bluetoothLeScanner

    private var scanCallback: MyonnaiseScanCallback? = null
    private var connectCallback: MyonnaiseConnectCallback? = null

    fun startScan(): Flowable<BluetoothDevice> {
        val scanFlowable: Flowable<BluetoothDevice> = Flowable.create({
            scanCallback = MyonnaiseScanCallback(it)
            blLowEnergyScanner.startScan(scanCallback)
        }, BackpressureStrategy.BUFFER)
        return scanFlowable.doOnCancel {
            println("Scan Stopped")
            blLowEnergyScanner.stopScan(scanCallback)
        }
    }

    fun startScan(interval: Long, timeUnit: TimeUnit): Flowable<BluetoothDevice> =
            startScan().takeUntil(Flowable.timer(interval, timeUnit))

    fun connect(bluetoothDevice: BluetoothDevice): Single<Myo> =
            Single.create { it.onSuccess(connectToGatt(bluetoothDevice)) }

    fun connect(myoAddress: String): Single<Myo> {
        return Single.create {
            connectCallback = MyonnaiseConnectCallback(it, myoAddress)
            println("Connect starting scan")
            blLowEnergyScanner.startScan(connectCallback)
        }
    }

    private fun connectToGatt(bluetoothDevice: BluetoothDevice): Myo {
        val gattCallback = MyoGattCallback()
        val gatt = bluetoothDevice.connectGatt(context, false, gattCallback)
        return Myo(gatt, gattCallback)
    }

    inner class MyonnaiseConnectCallback(
            private val emitter: SingleEmitter<Myo>,
            private val myoAddress: String
    ) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            println("Connect: On Scan Result ${result?.rssi} ${result?.toString()}")
            result?.device?.apply {
                if (this.address == myoAddress) {
                    blLowEnergyScanner.stopScan(this@MyonnaiseConnectCallback)
                    emitter.onSuccess(connectToGatt(this))
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println("Connect: On Scan Failed")
            emitter.onError(RuntimeException())
        }
    }

    inner class MyonnaiseScanCallback(private val emitter: FlowableEmitter<BluetoothDevice>) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            println("On Scan Result ${result?.rssi} ${result?.toString()}")
            result?.device?.apply { emitter.onNext(this) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            println("On Scan Failed")
            emitter.onError(RuntimeException())
        }
    }
}

class Myo(gatt: BluetoothGatt, gattCallback: MyoGattCallback) {

}
