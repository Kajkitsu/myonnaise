package com.ncorti.myonnaise

import java.util.UUID

/** Service ID - MYO CONTROL  */
val SERVICE_CONTROL_ID: UUID = UUID.fromString("d5060001-a904-deb9-4748-2c7f4a124842")
/** Service ID - MYO EMG DATA  */
val SERVICE_EMG_DATA_ID: UUID = UUID.fromString("d5060005-a904-deb9-4748-2c7f4a124842")
/** Service ID - MYO IMU DATA  */
val SERVICE_IMU_DATA_ID: UUID = UUID.fromString("d5060002-a904-deb9-4748-2c7f4a124842")
/** Service ID - MYO CLASSIFIER  */
val SERVICE_CLASSIFIER_ID: UUID = UUID.fromString("d5060003-a904-deb9-4748-2c7f4a124842")
/** Service ID - MYO BATTERY  */
val SERVICE_BATTERY_ID: UUID = UUID.fromString("d506180f-a904-deb9-4748-2c7f4a124842")

/** Characteristics ID - Myo Information  */
val CHAR_INFO_ID: UUID = UUID.fromString("d5060101-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - Myo Firmware */
val CHAR_FIRMWARE_ID: UUID = UUID.fromString("d5060201-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - Command ID  */
val CHAR_COMMAND_ID: UUID = UUID.fromString("d5060401-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - IMU Data  */
val CHAR_IMU_DATA_ID: UUID = UUID.fromString("d5060402-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - Motion Event ID  */
val CHAR_MOTION_EVENT_ID: UUID = UUID.fromString("d5060502-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - Classifier Event ID  */
val CHAR_CLASSIFIER_EVENT_ID: UUID = UUID.fromString("d5060103-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - Battery Level ID  */
val CHAR_BATTERY_LEVEL_ID: UUID = UUID.fromString("d5062a19-a904-deb9-4748-2c7f4a124842")

/** Characteristics ID - EMG Sample 0  */
val CHAR_EMG_0_ID: UUID = UUID.fromString("d5060105-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - EMG Sample 1  */
val CHAR_EMG_1_ID: UUID = UUID.fromString("d5060205-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - EMG Sample 2  */
val CHAR_EMG_2_ID: UUID = UUID.fromString("d5060305-a904-deb9-4748-2c7f4a124842")
/** Characteristics ID - EMG Sample 3  */
val CHAR_EMG_3_ID: UUID = UUID.fromString("d5060405-a904-deb9-4748-2c7f4a124842")

/** Data ID - Device name data  */
val DATA_DEVICE_NAME_ID: UUID = UUID.fromString("d5062a00-a904-deb9-4748-2c7f4a124842")


/** Postfix for all the EMG Characteristic.*/
const val CHAR_EMG_POSTFIX = "05-a904-deb9-4748-2c7f4a124842"

/**
 * Android Characteristic ID
 * (from Android Samples/BluetoothLeGatt/SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
 */
val CHAR_CLIENT_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

/** Myo Array Size. The device is sending arrays with 16 bytes in it every time. */
const val EMG_ARRAY_SIZE = 16

/** Max Myo Frequency (200Hz) */
const val MYO_MAX_FREQUENCY = 200

/** Number of Myo EMG Channels */
const val MYO_CHANNELS = 18

/** Max Myo Value. This is used mostly for graphical purposes */
const val MYO_MAX_VALUE = 150.0f

/** Min Myo Value. This is used mostly for graphical purposes */
const val MYO_MIN_VALUE = -150.0f

/** Keep Alive in MS. We will send an [CommandList.unSleep] command every [KEEP_ALIVE_INTERVAL_MS] */
const val KEEP_ALIVE_INTERVAL_MS = 10000

/** Myo comand list - Vibration */
const val COMAND_VIBRATION_NONE = 0
const val COMAND_VIBRATION_SHORT = 1
const val COMAND_VIBRATION_MEDIUM = 2
const val COMAND_VIBRATION_LONG = 3

/** Myo comand list - Emg */
/** No emg data is delivered */
const val COMAND_EMG_NONE = 0

/** EMG data with powerline interface being filtered out.*/
const val COMAND_EMG_FILTERED = 2

/** Raw unfiltered EMG data, this mode will implicitly set {@link eu.darken.myolib.MyoCmds.ClassifierMode#DISABLED}*/
const val COMAND_EMG_RAW = 3


/** Myo comand list - Imu */
/**Do not send IMU data or events.*/
const val COMAND_IMU_NONE = 0

/**Send IMU data streams (accelerometer, gyroscope, and orientation).*/
const val COMAND_IMU_DATA = 1

/** Send motion events detected by the IMU (e.g. taps).*/
const val COMAND_IMU_EVENTS = 2

/** Send both IMU data streams and motion events..*/
const val COMAND_IMU_ALL = 3

/** Send raw IMU data streams. */
const val COMAND_IMU_RAW = 4


/** Myo comand list - CLassifierMode */
/** Disable and reset the internal state of the onboard classifier. */
const val COMAND_CLASSIFIER_DISABLE = 0

/** Send classifier events (poses and arm events).*/
const val COMAND_CLASSIFIER_ENABLE = 1

/** Myo comand list - SleepMode */
/** Go to sleep/standby after a few seconds of inactivity.*/
const val COMAND_SLEEP_MODE_NORMAL = 0
/** Never go into sleep/standby while the device is connected. */
const val COMAND_SLEEP_MODE_NEVER = 1




internal const val TAG = "MYO"

