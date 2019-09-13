package com.ncorti.myonnaise.MyoCompoments

/*
class Emg(device: BluetoothDevice) : Myo(device) /*{
    /**
     * Get a [Flowable] where you can receive data from the device.
     * Data is delivered as a FloatArray of size [MYO_CHANNELS].
     * If frequency is set (!= 0) then sub-sampling is performed to achieve the desired frequency.
     */
    fun dataFlowableEmg(): Flowable<FloatArray> {
        return if (frequency == 0) {
            dataEmgProcessor
        } else {
            dataEmgProcessor.sample((1000 / frequency).toLong(), TimeUnit.MILLISECONDS)
        }
    }

    private fun FindGattServiceEmg(gatt: BluetoothGatt) {
        serviceEmg = gatt.getService(SERVICE_EMG_DATA_ID)
        serviceEmg?.apply {
            characteristicEmg0 = serviceEmg?.getCharacteristic(CHAR_EMG_0_ID)
            characteristicEmg1 = serviceEmg?.getCharacteristic(CHAR_EMG_1_ID)
            characteristicEmg2 = serviceEmg?.getCharacteristic(CHAR_EMG_2_ID)
            characteristicEmg3 = serviceEmg?.getCharacteristic(CHAR_EMG_3_ID)

            val emgCharacteristics = listOf(
                    characteristicEmg0,
                    characteristicEmg1,
                    characteristicEmg2,
                    characteristicEmg3
            )

            emgCharacteristics.forEach { emgCharacteristic ->
                emgCharacteristic?.apply {
                    if (gatt.setCharacteristicNotification(emgCharacteristic, true)) {
                        val descriptor = emgCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG)
                        descriptor?.apply {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            writeDescriptor(gatt, descriptor)
                        }
                    }
                }
            }
        }
    }

    private fun putEmgDataToDataProcessor(characteristic: BluetoothGattCharacteristic) {
        val emgData = characteristic.value
        byteReaderEmg.byteData = emgData
        Log.d(TAG, "emgData.size) "+emgData.size)
        // We receive 16 bytes of data. Let's cut them in 2 and deliver both of them.
        dataEmgProcessor.onNext(byteReaderEmg.getBytes(EMG_ARRAY_SIZE / 2))
        dataEmgProcessor.onNext(byteReaderEmg.getBytes(EMG_ARRAY_SIZE / 2))
        //
    }
}

*/
        */
