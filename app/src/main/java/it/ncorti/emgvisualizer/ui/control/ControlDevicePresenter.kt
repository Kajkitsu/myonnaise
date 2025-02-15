@file:Suppress("ComplexMethod")

package it.ncorti.emgvisualizer.ui.control

import com.ncorti.myonnaise.*
import com.ncorti.myonnaise.MyoCompoments.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import it.ncorti.emgvisualizer.dagger.DeviceManager

class ControlDevicePresenter(
    override val view: ControlDeviceContract.View,
    private val myonnaise: Myonnaise,
    private val deviceManager: DeviceManager
) : ControlDeviceContract.Presenter(view) {

    internal var statusSubscription: Disposable? = null
    internal var controlSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        if (deviceManager.selectedIndex == -1) {
            view.disableConnectButton()
            return
        }

        val selectedDevice = deviceManager.scannedDeviceList[deviceManager.selectedIndex]

        view.showDeviceInformation(selectedDevice.name, selectedDevice.address)
        view.enableConnectButton()

        if (deviceManager.myo == null) {
            deviceManager.myo = myonnaise.getMyo(selectedDevice)
        }

        deviceManager.myo?.apply {
            statusSubscription =
                this.statusObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        when (it) {
                            MyoStatus.CONNECTED -> {
                                view.hideConnectionProgress()
                                view.showConnected()
                            }
                            MyoStatus.CONNECTING -> {
                                view.showConnectionProgress()
                                view.showConnecting()
                            }
                            MyoStatus.READY -> {
                                view.enableControlPanel()
                            }
                            else -> {
                                view.hideConnectionProgress()
                                view.showDisconnected()
                                view.disableControlPanel()
                            }
                        }
                    }
            controlSubscription =
                this.controlObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it == MyoControlStatus.STREAMING) {
                            view.showStreaming()
                        } else {
                            view.showNotStreaming()
                        }
                    }
        }
    }

    override fun stop() {
        statusSubscription?.dispose()
        controlSubscription?.dispose()
    }

    override fun onConnectionToggleClicked() {
        deviceManager.myo?.apply {
            if (!this.isConnected()) {
                this.connect(myonnaise.context)
            } else {
                this.disconnect()
            }
        }
    }

    override fun onStreamingToggleClicked() {
        deviceManager.myo?.apply {
            if (!this.isStreaming()) {
                this.sendCommand(CommandList.setStreaming(emgMode = EmgModeType.FILTERED, imuMode = ImuModeType.DATA)
                        //CommandList.emgFilteredOnly()
                         )
            } else {
                this.sendCommand(CommandList.setStreaming(emgMode = EmgModeType.NONE,imuMode = ImuModeType.NONE,classMode = ClassifierMode.DISABLE))
            }
        }
    }

    override fun onVibrateClicked(duration: Int) {
        deviceManager.myo?.apply {
            this.sendCommand(
                when (duration) {
                    1 -> CommandList.vibration(VibrationType.SHORT)
                    2 -> CommandList.vibration(VibrationType.MEDIUM)
                    else -> CommandList.vibration(VibrationType.LONG)
                }
            )
        }
    }

    @Suppress("MagicNumber")
    override fun onProgressSelected(progress: Int) {
        val selectedFrequency = when (progress) {
            0 -> 1
            1 -> 2
            2 -> 5
            3 -> 10
            4 -> 25
            5 -> 50
            6 -> 100
            else -> MYO_MAX_FREQUENCY
        }
        view.showFrequency(selectedFrequency)
        deviceManager.myo?.apply {
            this.frequency = selectedFrequency
        }
    }
}
