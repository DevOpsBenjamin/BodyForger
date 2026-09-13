package app.bodyforger.wear.tile

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import app.bodyforger.wear.R
import app.bodyforger.wear.presentation.WearMainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class BodyForgerTileService : TileService() {

    companion object {
        const val EXTRA_NAV_TARGET = "EXTRA_NAV_TARGET"
        const val TARGET_WORKOUT = "workout"
        const val TARGET_WEIGH_IN = "weigh_in"
        const val RESOURCES_VERSION = "1"
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val layout = buildTileLayout(requestParams)
        val timelineEntry = TimelineBuilders.TimelineEntry.Builder()
            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(layout).build())
            .build()
        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(timelineEntry)
            .build()

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline)
            .setFreshnessIntervalMillis(60_000L)
            .build()

        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        val resources = ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
        return Futures.immediateFuture(resources)
    }

    private fun buildTileLayout(requestParams: RequestBuilders.TileRequest): LayoutElementBuilders.LayoutElement {
        val context = this
        val deviceParams = requestParams.deviceConfiguration

        val workoutClick = ModifiersBuilders.Clickable.Builder()
            .setId("start_workout")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName(WearMainActivity::class.java.name)
                            .addKeyToExtraMapping(
                                EXTRA_NAV_TARGET,
                                ActionBuilders.stringExtra(TARGET_WORKOUT)
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        val weighInClick = ModifiersBuilders.Clickable.Builder()
            .setId("start_weigh_in")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName(WearMainActivity::class.java.name)
                            .addKeyToExtraMapping(
                                EXTRA_NAV_TARGET,
                                ActionBuilders.stringExtra(TARGET_WEIGH_IN)
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        val workoutChip = Chip.Builder(context, workoutClick, deviceParams)
            .setPrimaryLabelContent(getString(R.string.action_start_workout))
            .setChipColors(
                ChipColors(
                    ColorBuilders.argb(0xFFCCFF00.toInt()), // NeonLime
                    ColorBuilders.argb(0xFF000000.toInt())  // Black text
                )
            )
            .build()

        val weighInChip = CompactChip.Builder(
            context,
            getString(R.string.action_start_weigh_in),
            weighInClick,
            deviceParams
        )
            .setChipColors(
                ChipColors(
                    ColorBuilders.argb(0xFF1E2024.toInt()), // Dark Card background
                    ColorBuilders.argb(0xFF00F0FF.toInt())  // ElectricCyan text
                )
            )
            .build()

        val contentColumn = LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(workoutChip)
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(6f)).build())
            .addContent(weighInChip)
            .build()

        return PrimaryLayout.Builder(deviceParams)
            .setPrimaryLabelTextContent(
                Text.Builder(context, getString(R.string.tile_header))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(0xFF00F0FF.toInt()))
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, getString(R.string.tile_status_ready))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(0xFF8E8E93.toInt()))
                    .build()
            )
            .setContent(contentColumn)
            .build()
    }
}
