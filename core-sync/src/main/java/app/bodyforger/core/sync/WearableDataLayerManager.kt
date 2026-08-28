package app.bodyforger.core.sync

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable

class WearableDataLayerManager(private val context: Context) {
    val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
}
