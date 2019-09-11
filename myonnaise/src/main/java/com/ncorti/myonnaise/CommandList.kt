@file:Suppress("MagicNumber")

package com.ncorti.myonnaise

import java.util.Arrays

typealias Command = ByteArray

/**
 * List of commands you can send to a [Myo] via the [Myo.sendCommand] method.
 * A [Command] is basically a [ByteArray] with all the bytes properly set.
 *
 * This is defined according to the Myo's Bluetooth specs defined here:
 * https://github.com/thalmiclabs/myo-bluetooth
 */
object CommandList {

    /** Send a vibration
     * by defult args it's "NONE" vibration comand
     * */
    fun vibration(vibrationType: Int = COMAND_VIBRATION_NONE): Command {
        val command_vibrate = 0x03.toByte()
        val payload_vibrate = 1.toByte()
        val vibrate_type = vibrationType.toByte()
        return byteArrayOf(command_vibrate, payload_vibrate, vibrate_type)
    }

    /** Setup the Streaming from the device
     * by defult args it stoping all streaming
     * */
    fun setStreaming(emgMode: Int = COMAND_EMG_NONE, imuMode: Int = COMAND_IMU_NONE, classMode : Int = COMAND_CLASSIFIER_DISABLE ): Command {
        val command_data = 0x01.toByte()
        val payload_data = 3.toByte()
        val emg_mode = emgMode.toByte()
        val imu_mode = imuMode.toByte()
        val class_mode = classMode.toByte()
        return byteArrayOf(command_data, payload_data, emg_mode, imu_mode, class_mode)
    }

    /** Set up a sleep mode
     * by defult args it's normal sleep mode
     * */
    fun sleepMode(sleepMode: Int = COMAND_SLEEP_MODE_NORMAL) : Command {
        val command_sleep_mode = 0x09.toByte()
        val payload_unlock = 1.toByte()
        val sleep_mode = sleepMode.toByte()
        return byteArrayOf(command_sleep_mode, payload_unlock, sleep_mode)
    }

    /** Stop all the Streaming from the device (EMG, IMU and Classifier) */
    fun stopStreaming(): Command {
        return setStreaming(emgMode = COMAND_EMG_NONE, imuMode = COMAND_IMU_NONE, classMode = COMAND_CLASSIFIER_DISABLE)
    }

    /** Start the EMG Streaming (filtered) */
    fun emgFilteredOnly(): Command {
        return setStreaming(emgMode = COMAND_EMG_FILTERED)
    }

    /** Start the EMG Streaming (unfiltered) */
    fun emgUnfilteredOnly(): Command {
        return setStreaming(emgMode = COMAND_EMG_RAW)
    }

    /** Send a short vibration */
    fun vibrationShort(): Command {
        return vibration(vibrationType = COMAND_VIBRATION_SHORT)
    }

    /** Send a medium vibration */
    fun vibrationMedium(): Command {
        return vibration(vibrationType = COMAND_VIBRATION_MEDIUM)
    }

    /** Send a long vibration */
    fun vibrationLong(): Command {
        return vibration(vibrationType = COMAND_VIBRATION_LONG)
    }

    /** Send an unsleep command. Needed to keep the Myo awake */
    fun unSleep(): Command {
        return sleepMode(sleepMode = COMAND_SLEEP_MODE_NEVER)
    }

    /** Send a normal sleep command. The myo will sleep if no interaction are recorded */
    fun normalSleep(): Command {
        return sleepMode(sleepMode = COMAND_SLEEP_MODE_NORMAL)
    }
}

/** Extension function to check the [Command] is a generic "start streaming" command. */
fun Command.isStartStreamingCommand() =
    this.size >= 4 &&
        this[0] == 0x01.toByte() &&
        (this[2] != 0x00.toByte() || this[3] != 0x00.toByte() || this[4] != 0x00.toByte())

/** Extension function to check the [Command] is a stop streaming command */
fun Command.isStopStreamingCommand() = Arrays.equals(this, CommandList.stopStreaming())
