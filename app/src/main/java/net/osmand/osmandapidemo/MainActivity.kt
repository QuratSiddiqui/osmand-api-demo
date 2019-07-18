package main.java.net.osmand.osmandapidemo

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.title_desc_list_layout.view.*
import main.java.net.osmand.osmandapidemo.CloseAfterCommandDialogFragment.ActionType
import main.java.net.osmand.osmandapidemo.CloseAfterCommandDialogFragment.Companion.ACTION_CODE_KEY
import main.java.net.osmand.osmandapidemo.MainActivity.Companion.CITIES
import main.java.net.osmand.osmandapidemo.OpenGpxDialogFragment.Companion.SEND_AS_RAW_DATA_REQUEST_CODE_KEY
import main.java.net.osmand.osmandapidemo.OpenGpxDialogFragment.Companion.SEND_AS_URI_REQUEST_CODE_KEY
import net.osmand.aidl.gpx.StartGpxRecordingParams
import net.osmand.aidl.gpx.StopGpxRecordingParams
import net.osmand.aidl.map.ALatLon
import net.osmand.aidl.maplayer.point.AMapPoint
import net.osmand.aidl.search.SearchParams
import net.osmand.aidl.search.SearchResult
import net.osmand.osmandapidemo.R
import java.io.*

class MainActivity : AppCompatActivity(), OsmAndHelper.OnOsmandMissingListener {

    companion object {
        private const val TAG = "MainActivity"
        const val REQUEST_OSMAND_API = 1001
        const val REQUEST_NAVIGATE_GPX_RAW_DATA = 1002
        const val REQUEST_SHOW_GPX_RAW_DATA = 1003
        const val REQUEST_NAVIGATE_GPX_URI = 1004
        const val REQUEST_SHOW_GPX_URI = 1005
        const val REQUEST_SHOW_GPX_RAW_DATA_AIDL = 1006
        const val REQUEST_SHOW_GPX_URI_AIDL = 1007
        const val REQUEST_NAVIGATE_GPX_RAW_DATA_AIDL = 1008
        const val REQUEST_NAVIGATE_GPX_URI_AIDL = 1009
        const val AUTHORITY = "net.osmand.osmandapidemo.fileprovider"
        const val GPX_FILE_NAME = "aild_test.gpx"
        
        const val KEY_UPDATES_LISTENER = "subscribe_for_updates"
        const val KEY_OSMAND_INIT_LISTENER = "on_osmand_init"
        const val KEY_GPX_BITMAP_LISTENER = "on_bitmap_created"
        const val KEY_NAV_INFO_LISTENER = "on_nav_info_update"
        const val KEY_NAV_CONTEXT_BTN_LISTENER = "on_ctx_btn_click"
        
        
        val CITIES = arrayOf(
                Location("Bruxelles - Brussel", 50.8465565, 4.351697, 50.83477, 4.4068823),
                Location("London", 51.5073219, -0.1276474, 51.52753, -0.07244986),
                Location("Paris", 48.8566101, 2.3514992, 48.87588, 2.428313),
                Location("Budapest", 47.4983815, 19.0404707, 47.48031, 19.067793),
                Location("Moscow", 55.7506828, 37.6174976, 55.769417, 37.698547),
                Location("Beijing", 39.9059631, 116.391248, 39.88707, 116.43207),
                Location("Tokyo", 35.6828378, 139.7589667, 35.72936, 139.703),
                Location("Washington", 38.8949549, -77.0366456, 38.91373, -77.02069),
                Location("Ottawa", 45.4210328, -75.6900219, 45.386864, -75.783356),
                Location("Panama", 8.9710438, -79.5340599, 8.992735, -79.5157),
                Location("Minsk", 53.9072394, 27.5863608, 53.9022545, 27.5619212),
                Location("Amsterdam", 52.3704312, 4.8904288, 52.3693012, 4.9013307)
        )

        val GPX_COLORS = arrayOf(
                "", "red", "orange", "lightblue", "blue", "purple",
                "translucent_red", "translucent_orange", "translucent_lightblue",
                "translucent_blue", "translucent_purple"
        )
    }

    private var counter = 1
    private var delay: Long = 5000
    var mOsmAndHelper: OsmAndHelper? = null
    private var mAidlHelper: OsmAndAidlHelper? = null

    private var progressDialog: ProgressDialog? = null
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    
    private val callbackKeys = mutableMapOf<String, Long>()

    enum class ApiActionType {
        UNDEFINED,

        AIDL_SET_NAV_DRAWER_ITEMS,

        AIDL_REFRESH_MAP,

        AIDL_ADD_FAVORITE_GROUP,
        AIDL_UPDATE_FAVORITE_GROUP,
        AIDL_REMOVE_FAVORITE_GROUP,

        AIDL_ADD_MAP_WIDGET,
        AIDL_UPDATE_MAP_WIDGET,
        AIDL_REMOVE_MAP_WIDGET,

        AIDL_ADD_FAVORITE,
        AIDL_UPDATE_FAVORITE,
        AIDL_REMOVE_FAVORITE,

        AIDL_ADD_MAP_MARKER,
        AIDL_UPDATE_MAP_MARKER,
        AIDL_REMOVE_MAP_MARKER,

        AIDL_ADD_MAP_LAYER,
        AIDL_REMOVE_MAP_LAYER,

		AIDL_SHOW_MAP_POINT,

        AIDL_ADD_MAP_POINT,
        AIDL_UPDATE_MAP_POINT,
        AIDL_REMOVE_MAP_POINT,

        AIDL_SHOW_GPX,
        AIDL_HIDE_GPX,
        AIDL_REMOVE_GPX,
        AIDL_START_GPX_REC,
        AIDL_STOP_GPX_REC,

        AIDL_TAKE_PHOTO,
        AIDL_START_VIDEO_REC,
        AIDL_START_AUDIO_REC,
        AIDL_STOP_REC,

        AIDL_SET_MAP_LOCATION,

        AIDL_NAVIGATE,
        AIDL_NAVIGATE_SEARCH,
        AIDL_REGISTER_FOR_NAV_UPDATES,
        AIDL_REGISTER_FOR_VOICE_ROUTE_MSGS,

        AIDL_PAUSE_NAVIGATION,
        AIDL_RESUME_NAVIGATION,
        AIDL_STOP_NAVIGATION,
        AIDL_MUTE_NAVIGATION,
        AIDL_UNMUTE_NAVIGATION,

        AIDL_SEARCH,
        
        AIDL_REGISTER_FOR_UPDATES,
        AIDL_UNREGISTER_FORM_UPDATES,
        
        AIDL_SET_NAV_DRAWER_LOGO,
        AIDL_SET_NAV_DRAWER_FOOTER,
        AIDL_SET_ENABLED_UI_IDS,
        AIDL_SET_DISABLED_UI_IDS,
        AIDL_SET_ENABLED_MENU_PATTERNS,
        AIDL_SET_DISABLED_MENU_PATTERNS,
        AIDL_REG_WIDGET_VISIBILITY,
        AIDL_REG_WIDGET_AVAILABILITY,
        AIDL_CHANGE_PLUGIN_STATE,
        AIDL_CUSTOMIZE_OSMAND_SETTINGS,
        AIDL_RESTORE_OSMAND_SETTINGS,
        AIDL_CHECK_OSMAND_CUSTOMIZATION,
        AIDL_ADD_CONTEXT_MENU_BTN,
        AIDL_REMOVE_CONTEXT_MENU_BTN,
        
        AIDL_GET_IMPORTED_GPX_FILES,
        AIDL_GET_SQLITEDB_FILES,
        AIDL_GET_ACTIVE_SQLITEDB_FILES,
        AIDL_SHOW_SQLITEDB_FILE,
        AIDL_HIDE_SQLITEDB_FILE,
        AIDL_GET_BITMAP_FOR_GPX,
        AIDL_COPY_FILE_TO_OSMAND,
        
        
        INTENT_ADD_FAVORITE,
        INTENT_ADD_MAP_MARKER,

        INTENT_SHOW_LOCATION,

        INTENT_TAKE_PHOTO,
        INTENT_START_VIDEO_REC,
        INTENT_START_AUDIO_REC,

        INTENT_NAVIGATE,
        INTENT_NAVIGATE_SEARCH,

        INTENT_PAUSE_NAVIGATION,
        INTENT_RESUME_NAVIGATION,
        INTENT_STOP_NAVIGATION,
        INTENT_MUTE_NAVIGATION,
        INTENT_UNMUTE_NAVIGATION
    }

    fun execApiAction(apiActionType: ApiActionType, delayed: Boolean = true, location: Location? = null) {
        if (location != null) {
            lastLatitude = location.lat
            lastLongitude = location.lon
        }
        if (delayed) {
            Handler().postDelayed({
                execApiActionImpl(apiActionType, location)
            }, delay)
        } else {
            execApiActionImpl(apiActionType, location)
        }
    }

    private fun execApiActionImpl(apiActionType: ApiActionType, location: Location? = null) {
        val aidlHelper = mAidlHelper
        val osmandHelper = mOsmAndHelper
        if (aidlHelper != null && osmandHelper != null) {
            when (apiActionType) {
                ApiActionType.AIDL_SET_NAV_DRAWER_ITEMS -> {
                    aidlHelper.setNavDrawerItems(
                        packageName,
                        listOf(getString(R.string.app_name)),
                        listOf("osmand_api_demo://main_activity"),
                        listOf("ic_action_travel"),
                        listOf(-1)
                    )
                }
                ApiActionType.AIDL_REFRESH_MAP -> {
                    aidlHelper.refreshMap()
                }
                ApiActionType.AIDL_ADD_FAVORITE_GROUP -> {
                    aidlHelper.addFavoriteGroup("New group", "purple", false)
                }
                ApiActionType.AIDL_UPDATE_FAVORITE_GROUP -> {
                    aidlHelper.updateFavoriteGroup("New group", "purple", false, "New group 1", "red", true)
                }
                ApiActionType.AIDL_REMOVE_FAVORITE_GROUP -> {
                    aidlHelper.removeFavoriteGroup("New group")
                }
                ApiActionType.AIDL_REGISTER_FOR_UPDATES -> {
                    aidlHelper.setUpdateListener { object:OsmAndAidlHelper.UpdateListener {
                        override fun onUpdatePing() {
                            Toast.makeText(this@MainActivity, "Ping from OsmAnd every 7 sec", Toast.LENGTH_SHORT).show()
                        }
                    } }
                    callbackKeys.put(KEY_UPDATES_LISTENER, aidlHelper.registerForUpdates(7000))
                }
                
                ApiActionType.AIDL_UNREGISTER_FORM_UPDATES -> {
                    if (callbackKeys.containsKey(KEY_UPDATES_LISTENER)) {
                        aidlHelper.unregisterFromUpdates(callbackKeys[KEY_UPDATES_LISTENER]!!)
                        callbackKeys.remove(KEY_UPDATES_LISTENER)
                        Toast.makeText(this@MainActivity, "Unsubscribed from OsmAnd pings", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "You need first to subscribe for updates from OsmAnd", Toast.LENGTH_SHORT).show()
                    }
                    
                }
                else -> Unit
            }
            // location depended types
            if (location != null) {
                when (apiActionType) {
                    ApiActionType.AIDL_ADD_FAVORITE -> {
                        aidlHelper.addFavorite(location.lat, location.lon, location.name,
                                location.name + " city", "Cities", "red", true)
                    }
                    ApiActionType.AIDL_UPDATE_FAVORITE -> {
                        aidlHelper.updateFavorite(location.lat, location.lon, location.name, "Cities",
                                location.lat, location.lon, location.name, location.name + " city", "Cities", "yellow", true)
                    }
                    ApiActionType.AIDL_REMOVE_FAVORITE -> {
                        aidlHelper.removeFavorite(location.lat, location.lon, location.name, "Cities")
                    }
                    ApiActionType.AIDL_ADD_MAP_MARKER -> {
                        aidlHelper.addMapMarker(location.lat, location.lon, location.name)
                    }
                    ApiActionType.AIDL_UPDATE_MAP_MARKER -> {
                        aidlHelper.updateMapMarker(location.lat, location.lon, location.name,
                                location.lat, location.lon, location.name + " " + counter++)
                    }
                    ApiActionType.AIDL_REMOVE_MAP_MARKER -> {
                        aidlHelper.removeMapMarker(location.lat, location.lon, location.name)
                    }
                    ApiActionType.AIDL_SHOW_MAP_POINT -> {
                        aidlHelper.showMapPoint(
                            "layer_1",
                            "id_" + location.name,
                            location.name.substring(0, 1),
                            location.name,
                            "City",
                            Color.GREEN,
                            ALatLon(location.lat, location.lon),
                            listOf("Big city", "Population: ..."),
							mapOf(AMapPoint.POINT_SPEED_PARAM to "4.0", AMapPoint.POINT_TYPE_ICON_NAME_PARAM to "ic_type_address")
                        )
                    }
                    ApiActionType.AIDL_ADD_MAP_POINT -> {
                        aidlHelper.addMapPoint(
                                "layer_1",
                                "id_" + location.name,
                                location.name.substring(0, 1),
                                location.name,
                                "City",
                                Color.GREEN,
                                ALatLon(location.lat, location.lon),
                                listOf("Big city", "Population: ..."),
								mapOf(AMapPoint.POINT_SPEED_PARAM to "4.0", AMapPoint.POINT_TYPE_ICON_NAME_PARAM to "ic_type_address")
						)
                    }
                    ApiActionType.AIDL_UPDATE_MAP_POINT -> {
                        aidlHelper.addMapPoint(
                                "layer_1",
                                "id_" + location.name,
                                location.name.substring(1, 2),
                                location.name,
                                "City",
                                Color.RED,
                                ALatLon(location.lat, location.lon),
                                listOf("Big city", "Population: unknown"),
								mapOf(AMapPoint.POINT_SPEED_PARAM to "4.0", AMapPoint.POINT_TYPE_ICON_NAME_PARAM to "ic_type_address")
						)
                    }
                    ApiActionType.AIDL_REMOVE_MAP_POINT -> {
                        aidlHelper.removeMapPoint("layer_1", "id_" + location.name)
                    }
                    ApiActionType.AIDL_TAKE_PHOTO -> {
                        aidlHelper.takePhotoNote(location.lat, location.lon)
                    }
                    ApiActionType.AIDL_START_VIDEO_REC -> {
                        aidlHelper.startVideoRecording(location.lat, location.lon)
                    }
                    ApiActionType.AIDL_START_AUDIO_REC -> {
                        aidlHelper.startAudioRecording(location.lat, location.lon)
                    }
                    ApiActionType.AIDL_SET_MAP_LOCATION -> {
                        aidlHelper.setMapLocation(location.lat, location.lon, 16, true)
                    }
                    ApiActionType.AIDL_NAVIGATE -> {
                        aidlHelper.navigate(location.name + " start",
                                location.latStart, location.lonStart,
                                location.name + " finish", location.lat, location.lon,
                                "bicycle", true)
                    }
                    ApiActionType.AIDL_NAVIGATE_SEARCH -> {
                        val alert = AlertDialog.Builder(this)
                        val editText = EditText(this)
                        alert.setTitle("Enter Search Query")
                        alert.setView(editText)
                        alert.setPositiveButton("Navigate") { _, _ ->
                            val text = editText.text.toString()
                            Handler().postDelayed({
                                aidlHelper.navigateSearch(location.name + " start",
                                        location.latStart, location.lonStart,
                                        text, location.latStart, location.lonStart,
                                        "car", true)
                            }, delay)
                        }
                        alert.setNegativeButton("Cancel", null)
                        alert.show()
                    }
                    ApiActionType.AIDL_PAUSE_NAVIGATION -> {
                        aidlHelper.pauseNavigation()
                    }
                    ApiActionType.AIDL_RESUME_NAVIGATION -> {
                        aidlHelper.resumeNavigation()
                    }
                    ApiActionType.AIDL_STOP_NAVIGATION -> {
                        aidlHelper.stopNavigation()
                    }
                    ApiActionType.AIDL_MUTE_NAVIGATION -> {
                        aidlHelper.muteNavigation()
                    }
                    ApiActionType.AIDL_UNMUTE_NAVIGATION -> {
                        aidlHelper.unmuteNavigation()
                    }
                    ApiActionType.AIDL_SEARCH -> {
                        val alert = AlertDialog.Builder(this)
                        val editText = EditText(this)
                        alert.setTitle("Enter Search Query")
                        alert.setView(editText)
                        alert.setPositiveButton("Search") { _, _ ->
                            progressDialog?.setTitle("Searching...")
                            progressDialog?.show()
                            val text = editText.text.toString()
                            aidlHelper.search(text, SearchParams.SEARCH_TYPE_ALL, location.latStart, location.lonStart, 1, 50)
                        }
                        alert.setNegativeButton("Cancel", null)
                        alert.show()
                    }
                    MainActivity.ApiActionType.INTENT_ADD_FAVORITE -> {
                        osmandHelper.addFavorite(location.lat, location.lon, location.name,
                                location.name + " city", "Cities", "red", true)
                    }
                    MainActivity.ApiActionType.INTENT_ADD_MAP_MARKER -> {
                        osmandHelper.addMapMarker(location.lat, location.lon, location.name)
                    }
                    MainActivity.ApiActionType.INTENT_SHOW_LOCATION -> {
                        osmandHelper.showLocation(location.lat, location.lon)
                    }
                    MainActivity.ApiActionType.INTENT_TAKE_PHOTO -> {
                        osmandHelper.takePhoto(location.lat, location.lon)
                    }
                    MainActivity.ApiActionType.INTENT_START_VIDEO_REC -> {
                        osmandHelper.recordVideo(location.lat, location.lon)
                    }
                    MainActivity.ApiActionType.INTENT_START_AUDIO_REC -> {
                        osmandHelper.recordAudio(location.lat, location.lon)
                    }
                    MainActivity.ApiActionType.INTENT_NAVIGATE -> {
                        osmandHelper.navigate(location.name + " start",
                                location.latStart, location.lonStart,
                                location.name + " finish", location.lat, location.lon,
                                "bicycle", true)
                    }
                    MainActivity.ApiActionType.INTENT_NAVIGATE_SEARCH -> {
                        val alert = AlertDialog.Builder(this)
                        val editText = EditText(this)
                        alert.setTitle("Enter Search Query")
                        alert.setView(editText)
                        alert.setPositiveButton("Navigate") { _, _ ->
                            val text = editText.text.toString()
                            osmandHelper.navigateSearch(location.name + " start",
                                    location.latStart, location.lonStart,
                                    text, location.latStart, location.lonStart,
                                    "car", true, true)
                        }
                        alert.setNegativeButton("Cancel", null)
                        alert.show()
                    }
                    MainActivity.ApiActionType.INTENT_PAUSE_NAVIGATION -> {
                        osmandHelper.pauseNavigation()
                    }
                    MainActivity.ApiActionType.INTENT_RESUME_NAVIGATION -> {
                        osmandHelper.resumeNavigation()
                    }
                    MainActivity.ApiActionType.INTENT_STOP_NAVIGATION -> {
                        osmandHelper.stopNavigation()
                    }
                    MainActivity.ApiActionType.INTENT_MUTE_NAVIGATION -> {
                        osmandHelper.muteNavigation()
                    }
                    MainActivity.ApiActionType.INTENT_UNMUTE_NAVIGATION -> {
                        osmandHelper.umuteNavigation()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mOsmAndHelper = OsmAndHelper(this, REQUEST_OSMAND_API, this)
        mAidlHelper = OsmAndAidlHelper(this.application, this)

        mAidlHelper!!.setSearchCompleteListener {
            runOnUiThread {
                progressDialog?.hide()
                showSearchResultsDialogFragment(it, lastLatitude, lastLongitude)
            }
        }

        progressDialog = ProgressDialog(this)

        setContentView(R.layout.activity_main)

        setDrawable(addFavoriteButton, R.drawable.ic_action_fav_dark)
        setDrawable(addMapMarkerButton, R.drawable.ic_action_flag_dark)
        setDrawable(showLocationButton, R.drawable.ic_action_flag_dark)
        setDrawable(startAudioRecButton, R.drawable.ic_action_micro_dark)
        setDrawable(startVideoRecButton, R.drawable.ic_action_video_dark)
        setDrawable(stopRecButton, R.drawable.ic_action_rec_stop)
        setDrawable(takePhotoButton, R.drawable.ic_action_photo_dark)
        setDrawable(startGpxRecButton, R.drawable.ic_action_play)
        setDrawable(stopGpxRecButton, R.drawable.ic_action_rec_stop)
        setDrawable(showGpxButton, R.drawable.ic_action_polygom_dark)
        setDrawable(navigateGpxButton, R.drawable.ic_action_gdirections_dark)
        setDrawable(navigateButton, R.drawable.ic_action_gdirections_dark)
        setDrawable(navigateSearchButton, R.drawable.ic_action_gdirections_dark)
        setDrawable(getInfoButton, R.drawable.ic_action_gabout_dark)
        setDrawable(pauseNavigationButton, R.drawable.ic_action_gdirections_dark)
        setDrawable(resumeNavigationButton, R.drawable.ic_action_gdirections_dark)
        setDrawable(stopNavigationButton, R.drawable.ic_action_rec_stop)
        setDrawable(muteNavigationButton, R.drawable.ic_action_micro_dark)
        setDrawable(unmuteNavigationButton, R.drawable.ic_action_micro_dark)

        // AIDL

        aidlSetNavDrawerItems.setOnClickListener {
            execApiAction(ApiActionType.AIDL_SET_NAV_DRAWER_ITEMS)
        }

        aidlRefreshMapButton.setOnClickListener {
            execApiAction(ApiActionType.AIDL_REFRESH_MAP)
        }

        aidlAddFavoriteGroupButton.setOnClickListener {
            execApiAction(ApiActionType.AIDL_ADD_FAVORITE_GROUP)
        }

        aidlUpdateFavoriteGroupButton.setOnClickListener {
            execApiAction(ApiActionType.AIDL_UPDATE_FAVORITE_GROUP)
        }

        aidlRemoveFavoriteGroupButton.setOnClickListener {
            execApiAction(ApiActionType.AIDL_REMOVE_FAVORITE_GROUP)
        }

        aidlAddFavoriteButton.setOnClickListener {
            showChooseLocationDialogFragment("Add favourite", ApiActionType.AIDL_ADD_FAVORITE)
        }

        aidlUpdateFavoriteButton.setOnClickListener {
            showChooseLocationDialogFragment("Update favourite", ApiActionType.AIDL_UPDATE_FAVORITE)
        }

        aidlRemoveFavoriteButton.setOnClickListener {
            showChooseLocationDialogFragment("Remove favourite", ApiActionType.AIDL_REMOVE_FAVORITE)
        }

        aidlAddMapMarkerButton.setOnClickListener {
            showChooseLocationDialogFragment("Add map marker", ApiActionType.AIDL_ADD_MAP_MARKER)
        }

        aidlUpdateMapMarkerButton.setOnClickListener {
            showChooseLocationDialogFragment("Update map marker", ApiActionType.AIDL_UPDATE_MAP_MARKER)
        }

        aidlRemoveMapMarkerButton.setOnClickListener {
            showChooseLocationDialogFragment("Remove map marker", ApiActionType.AIDL_REMOVE_MAP_MARKER)
        }

        val startDemoIntent = packageManager.getLaunchIntentForPackage("net.osmand.osmandapidemo")
        startDemoIntent?.addCategory(Intent.CATEGORY_LAUNCHER)

        aidlAddFirstWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.addMapWidget(
                        "111",
                        "ic_action_speed",
                        "AIDL Speed",
                        "widget_speed_day",
                        "widget_speed_night",
                        "10", "km/h", 50, startDemoIntent)
            }, delay)
        }

        aidlAddSecondWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.addMapWidget(
                        "222",
                        "ic_action_time",
                        "AIDL Time",
                        "widget_time_day",
                        "widget_time_night",
                        getTimeStr(), "", 51, startDemoIntent)
            }, delay)
        }

        aidlModifyFirstWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.updateMapWidget(
                        "111",
                        "ic_action_speed",
                        "AIDL Speed",
                        "widget_speed_day",
                        "widget_speed_night",
                        "1" + counter++, "km/h", 50, startDemoIntent)
            }, delay)
        }

        aidlModifySecondWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.updateMapWidget(
                        "222",
                        "ic_action_time",
                        "AIDL Time",
                        "widget_time_day",
                        "widget_time_night",
                        getTimeStr(), "", 51, startDemoIntent)
            }, delay)
        }

        aidlRemoveFirstWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.removeMapWidget("111")
            }, delay)
        }

        aidlRemoveSecondWidgetButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.removeMapWidget("222")
            }, delay)
        }

        aidlAddLayerButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.addMapLayer("layer_1", "OSMO Layer", 5.5f, null, true)
            }, delay)
        }

        aidlRemoveLayerButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.removeMapLayer("layer_1")
            }, delay)
        }

		aidlShowMapPoint.setOnClickListener {
			showChooseLocationDialogFragment("Show map point", ApiActionType.AIDL_SHOW_MAP_POINT)
		}

        aidlAddPointButton.setOnClickListener {
            showChooseLocationDialogFragment("Add map point", ApiActionType.AIDL_ADD_MAP_POINT)
        }

        aidlUpdatePointButton.setOnClickListener {
            showChooseLocationDialogFragment("Update map point", ApiActionType.AIDL_UPDATE_MAP_POINT)
        }

        aidlRemovePointButton.setOnClickListener {
            showChooseLocationDialogFragment("Remove map point", ApiActionType.AIDL_REMOVE_MAP_POINT)
        }

        aidlImportGpxButton.setOnClickListener {
            val args = Bundle()
            args.putInt(SEND_AS_RAW_DATA_REQUEST_CODE_KEY, REQUEST_SHOW_GPX_RAW_DATA_AIDL)
            args.putInt(SEND_AS_URI_REQUEST_CODE_KEY, REQUEST_SHOW_GPX_URI_AIDL)
            val openGpxDialogFragment = OpenGpxDialogFragment()
            openGpxDialogFragment.arguments = args
            openGpxDialogFragment.show(supportFragmentManager, OpenGpxDialogFragment.TAG)
        }

        aidlShowGpxButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.showGpx(GPX_FILE_NAME)
            }, delay)
        }

        aidlHideGpxButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.hideGpx(GPX_FILE_NAME)
            }, delay)
        }

        aidlGetActiveGpxButton.setOnClickListener {
            val activeGpxFiles = mAidlHelper!!.activeGpxFiles
            val sb = StringBuilder()
            if (activeGpxFiles != null) {
                for (gpxFile in activeGpxFiles) {
                    if (sb.isNotEmpty()) {
                        sb.append("<br>")
                    }
                    sb.append(gpxFile.fileName)
                }
            }
            if (sb.isEmpty()) {
                sb.append("No active files found")
            }
            val args = Bundle()
            args.putString(OsmAndInfoDialog.INFO_KEY, sb.toString())
            val infoDialog = OsmAndInfoDialog()
            infoDialog.arguments = args
            supportFragmentManager.beginTransaction()
                    .add(infoDialog, null).commitAllowingStateLoss()
        }

        aidlRemoveGpxButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.removeGpx(GPX_FILE_NAME)
            }, delay)
        }

        aidlSetMapLocationButton.setOnClickListener {
            showChooseLocationDialogFragment("Set map location", ApiActionType.AIDL_SET_MAP_LOCATION)
        }

        aidlStartGpxRecordingButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.startGpxRecording(StartGpxRecordingParams())
            }, delay)
        }

        aidlStopGpxRecordingButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.stopGpxRecording(StopGpxRecordingParams())
            }, delay)
        }

        aidlTakePhotoButton.setOnClickListener {
            showChooseLocationDialogFragment("Take photo", ApiActionType.AIDL_TAKE_PHOTO)
        }

        aidlStartVideoRecButton.setOnClickListener {
            showChooseLocationDialogFragment("Start video recording", ApiActionType.AIDL_START_VIDEO_REC)
        }

        aidlStartAudioRecButton.setOnClickListener {
            showChooseLocationDialogFragment("Start audio recording", ApiActionType.AIDL_START_AUDIO_REC)
        }

        aidlStopRecButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.stopRecording()
            }, delay)
        }

        aidlNavigateButton.setOnClickListener {
            showChooseLocationDialogFragment("Navigate to", ApiActionType.AIDL_NAVIGATE)
        }

        aidlPauseNavigationButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.pauseNavigation()
            }, delay)
        }

        aidlResumeNavigationButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.resumeNavigation()
            }, delay)
        }

        aidlStopNavigationButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.stopNavigation()
            }, delay)
        }

        aidlMuteNavigationButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.muteNavigation()
            }, delay)
        }

        aidlUnmuteNavigationButton.setOnClickListener {
            Handler().postDelayed({
                mAidlHelper!!.unmuteNavigation()
            }, delay)
        }

        aidlNavigateGpxButton.setOnClickListener {
            val args = Bundle()
            args.putInt(SEND_AS_RAW_DATA_REQUEST_CODE_KEY, REQUEST_NAVIGATE_GPX_RAW_DATA_AIDL)
            args.putInt(SEND_AS_URI_REQUEST_CODE_KEY, REQUEST_NAVIGATE_GPX_URI_AIDL)
            val openGpxDialogFragment = OpenGpxDialogFragment()
            openGpxDialogFragment.arguments = args
            openGpxDialogFragment.show(supportFragmentManager, OpenGpxDialogFragment.TAG)
        }

        aidlSearchButton.setOnClickListener {
            showChooseLocationDialogFragment("Search here", ApiActionType.AIDL_SEARCH, false)
        }

        aidlNavigateSearchButton.setOnClickListener {
            showChooseLocationDialogFragment("Search and navigate to", ApiActionType.AIDL_NAVIGATE_SEARCH, false)
        }

        // Intents

        addFavoriteButton.setOnClickListener {
            showChooseLocationDialogFragment("Add favourite", ApiActionType.INTENT_ADD_FAVORITE, false)
        }
        addMapMarkerButton.setOnClickListener {
            showChooseLocationDialogFragment("Add map marker", ApiActionType.INTENT_ADD_MAP_MARKER, false)
        }

        showLocationButton.setOnClickListener {
            showChooseLocationDialogFragment("Show location", ApiActionType.INTENT_SHOW_LOCATION, false)
        }

        startAudioRecButton.setOnClickListener {
            showChooseLocationDialogFragment("Start audio recording", ApiActionType.INTENT_START_AUDIO_REC, false)
        }
        startVideoRecButton.setOnClickListener {
            showChooseLocationDialogFragment("Start video recording", ApiActionType.INTENT_START_VIDEO_REC, false)
        }
        takePhotoButton.setOnClickListener {
            showChooseLocationDialogFragment("Take photo", ApiActionType.INTENT_TAKE_PHOTO, false)
        }
        stopRecButton.setOnClickListener { mOsmAndHelper!!.stopAvRec() }
        startGpxRecButton.setOnClickListener {
            val args = Bundle()
            args.putString(ACTION_CODE_KEY, ActionType.START_GPX_REC.name)
            val closeAfterCommandDialogFragment = CloseAfterCommandDialogFragment()
            closeAfterCommandDialogFragment.arguments = args
            closeAfterCommandDialogFragment.show(supportFragmentManager, CloseAfterCommandDialogFragment.TAG)
        }
        stopGpxRecButton.setOnClickListener {
            val args = Bundle()
            args.putString(ACTION_CODE_KEY, ActionType.STOP_GPX_REC.name)
            val closeAfterCommandDialogFragment = CloseAfterCommandDialogFragment()
            closeAfterCommandDialogFragment.arguments = args
            closeAfterCommandDialogFragment.show(supportFragmentManager, CloseAfterCommandDialogFragment.TAG)
        }
        showGpxButton.setOnClickListener {
            val args = Bundle()
            args.putInt(SEND_AS_RAW_DATA_REQUEST_CODE_KEY, REQUEST_SHOW_GPX_RAW_DATA)
            args.putInt(SEND_AS_URI_REQUEST_CODE_KEY, REQUEST_SHOW_GPX_URI)
            val openGpxDialogFragment = OpenGpxDialogFragment()
            openGpxDialogFragment.arguments = args
            openGpxDialogFragment.show(supportFragmentManager, OpenGpxDialogFragment.TAG)
        }
        navigateGpxButton.setOnClickListener {
            val args = Bundle()
            args.putInt(SEND_AS_RAW_DATA_REQUEST_CODE_KEY, REQUEST_NAVIGATE_GPX_RAW_DATA)
            args.putInt(SEND_AS_URI_REQUEST_CODE_KEY, REQUEST_NAVIGATE_GPX_URI)
            val openGpxDialogFragment = OpenGpxDialogFragment()
            openGpxDialogFragment.arguments = args
            openGpxDialogFragment.show(supportFragmentManager, OpenGpxDialogFragment.TAG)
        }
        navigateButton.setOnClickListener {
            showChooseLocationDialogFragment("Navigate to", ApiActionType.INTENT_NAVIGATE, false)
        }
        navigateSearchButton.setOnClickListener {
            showChooseLocationDialogFragment("Search and Navigate", ApiActionType.INTENT_NAVIGATE_SEARCH, false)
        }
        pauseNavigationButton.setOnClickListener { 
            mOsmAndHelper!!.pauseNavigation() 
        }
        
        resumeNavigationButton.setOnClickListener { 
            mOsmAndHelper!!.resumeNavigation() 
        }
        stopNavigationButton.setOnClickListener { mOsmAndHelper!!.stopNavigation() }
        muteNavigationButton.setOnClickListener { mOsmAndHelper!!.muteNavigation() }
        unmuteNavigationButton.setOnClickListener { mOsmAndHelper!!.umuteNavigation() }
        getInfoButton.setOnClickListener { mOsmAndHelper!!.getInfo() }
        aidlRegisterForUpdates.setOnClickListener { execApiActionImpl(ApiActionType.AIDL_REGISTER_FOR_UPDATES) }
        aidlUnregisterFromUpdates.setOnClickListener { execApiActionImpl(ApiActionType.AIDL_UNREGISTER_FORM_UPDATES) }
        setNavDrawerLogoWithParams.setOnClickListener {
            execApiActionImpl(ApiActionType.AIDL_SET_NAV_DRAWER_LOGO)
        }
        
        setNavDrawerFooterWithParams.setOnClickListener {
            execApiActionImpl(ApiActionType.AIDL_SET_NAV_DRAWER_FOOTER)
        }
        
        setEnabledIds.setOnClickListener {
            execApiActionImpl(ApiActionType.AIDL_SET_ENABLED_UI_IDS)
        }
        
        setDisabledIds.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_SET_DISABLED_UI_IDS) 
        }
        
        setEnabledPatterns.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_SET_ENABLED_MENU_PATTERNS)
        }
        
        setDisabledIds.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_SET_DISABLED_MENU_PATTERNS)
        }
        
        regWidgetAvailability.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_REG_WIDGET_AVAILABILITY)
        }

        regWidgetVisibility.setOnClickListener {
            execApiActionImpl(ApiActionType.AIDL_REG_WIDGET_VISIBILITY)
        }
        
        regWidgetAvailability.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_REG_WIDGET_AVAILABILITY)
        }
        
        changePluginState.setOnClickListener { 
            execApiActionImpl(ApiActionType.AIDL_CHANGE_PLUGIN_STATE)
        }
        
        customizeOsmandSettings.setOnClickListener {
            execApiActionImpl(ApiActionType.AIDL_CUSTOMIZE_OSMAND_SETTINGS)
        }

        
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OSMAND_API) {
            val sb = StringBuilder()
            sb.append("ResultCode = <b>").append(resultCodeStr(resultCode)).append("</b>")
            if (data != null) {
                val extras = data.extras
                if (extras != null && extras.size() > 0) {
                    for (key in data.extras.keySet()) {
                        val value = extras.get(key)
                        if (sb.isNotEmpty()) {
                            sb.append("<br>")
                        }
                        sb.append(key).append(" = <b>").append(value).append("</b>")
                    }
                }
            }
            val args = Bundle()
            args.putString(OsmAndInfoDialog.INFO_KEY, sb.toString())
            val infoDialog = OsmAndInfoDialog()
            infoDialog.arguments = args
            supportFragmentManager.beginTransaction()
                    .add(infoDialog, null).commitAllowingStateLoss()
        }
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_NAVIGATE_GPX_RAW_DATA -> {
                    handleGpxFile(data!!) { result -> mOsmAndHelper!!.navigateRawGpx(true, result) }
                }
                REQUEST_NAVIGATE_GPX_URI -> {
                    handleGpxUri(data!!) { result -> mOsmAndHelper!!.navigateGpxUri(true, result) }
                }
                REQUEST_SHOW_GPX_RAW_DATA -> {
                    handleGpxFile(data!!) { result -> mOsmAndHelper!!.showRawGpx(result) }
                }
                REQUEST_SHOW_GPX_URI -> {
                    handleGpxUri(data!!) { result -> mOsmAndHelper!!.showGpxUri(result) }
                }
                REQUEST_SHOW_GPX_RAW_DATA_AIDL -> {
                    Handler().postDelayed({
                        val color = GPX_COLORS[((GPX_COLORS.size - 1) * Math.random()).toInt()]
                        handleGpxFile(data!!) { data -> mAidlHelper!!.importGpxFromData(data, GPX_FILE_NAME, color, true) }
                    }, delay)
                }
                REQUEST_SHOW_GPX_URI_AIDL -> {
                    Handler().postDelayed({
                        val color = GPX_COLORS[((GPX_COLORS.size - 1) * Math.random()).toInt()]
                        handleGpxUri(data!!) { data -> mAidlHelper!!.importGpxFromUri(data, GPX_FILE_NAME, color, true) }
                    }, delay)
                }
                REQUEST_NAVIGATE_GPX_RAW_DATA_AIDL -> {
                    Handler().postDelayed({
                        handleGpxFile(data!!) { data -> mAidlHelper!!.navigateGpxFromData(data, true) }
                    }, delay)
                }
                REQUEST_NAVIGATE_GPX_URI_AIDL -> {
                    Handler().postDelayed({
                        handleGpxUri(data!!) { data -> mAidlHelper!!.navigateGpxFromUri(data, true) }
                    }, delay)
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        mAidlHelper!!.cleanupResources()
        super.onDestroy()
    }

    private fun handleGpxFile(data: Intent, action: (String) -> Unit) {
        try {
            val gpxParceDescriptor = contentResolver.openFileDescriptor(data.data, "r")
            val fileDescriptor = gpxParceDescriptor.fileDescriptor
            val inputStreamReader = InputStreamReader(FileInputStream(fileDescriptor))
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            bufferedReader.lineSequence().forEach { string: String -> stringBuilder.append(string) }
            inputStreamReader.close()
            action(stringBuilder.toString())
        } catch (e: Throwable) {
            Log.e(TAG, "", e)
        }
    }

    private fun handleGpxUri(data: Intent, action: (Uri) -> Unit) {
        try {
            val gpxParceDescriptor = contentResolver.openFileDescriptor(data.data, "r")
            val fileDescriptor = gpxParceDescriptor.fileDescriptor
            val inputStream = FileInputStream(fileDescriptor)
            val sharedDir = File(cacheDir, "share")
            if (!sharedDir.exists()) {
                sharedDir.mkdir()
            }
            val file = File(sharedDir, "shared.gpx")
            file.copyInputStreamToFile(inputStream)
            inputStream.close()
            val fileUri = FileProvider.getUriForFile(this, AUTHORITY, file)
            Log.d(TAG, "fileUri=$fileUri")
            Log.d(TAG, "file=" + file.readLines())
            action(fileUri)
        } catch (e: NullPointerException) {
            Log.e(TAG, "", e)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "", e)
        }
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                Log.d(TAG, input.copyTo(fileOut).toString())
            }
        }
    }

    override fun osmandMissing() {
        OsmAndMissingDialogFragment().show(supportFragmentManager, null)
    }

    private fun setDrawable(button: Button, drawableRes: Int) {
        val icon = ContextCompat.getDrawable(this, drawableRes)
        if (icon != null) {
            DrawableCompat.setTint(icon, ContextCompat.getColor(this, R.color.iconColor))
            val compatIcon = DrawableCompat.wrap(icon)
            DrawableCompat.setTint(compatIcon, ContextCompat.getColor(this, R.color.iconColor))
            button.setCompoundDrawablesWithIntrinsicBounds(compatIcon, null, null, null)
        }
    }

    private fun getTimeStr(): String {
        val time = System.currentTimeMillis()
        val seconds = (time / 1000) % 60
        val minutes = time / (1000 * 60) % 60
        val hours = time / (1000 * 60 * 60) % 24
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }

    private fun resultCodeStr(resultCode: Int): String {
        when (resultCode) {
            Activity.RESULT_OK -> return "OK"
            Activity.RESULT_CANCELED -> return "Canceled"
            Activity.RESULT_FIRST_USER -> return "First user"
            OsmAndHelper.RESULT_CODE_ERROR_UNKNOWN -> return "Unknown error"
            OsmAndHelper.RESULT_CODE_ERROR_NOT_IMPLEMENTED -> return "Feature is not implemented"
            OsmAndHelper.RESULT_CODE_ERROR_GPX_NOT_FOUND -> return "GPX not found"
            OsmAndHelper.RESULT_CODE_ERROR_INVALID_PROFILE -> return "Invalid profile"
            OsmAndHelper.RESULT_CODE_ERROR_PLUGIN_INACTIVE -> return "Plugin inactive"
        }
        return "" + resultCode
    }

    private fun showChooseLocationDialogFragment(title: String, apiActionType: ApiActionType, delayed: Boolean = true) {
        val args = Bundle()
        args.putString(ChooseLocationDialogFragment.TITLE_KEY, title)
        args.putString(ChooseLocationDialogFragment.API_ACTION_CODE_KEY, apiActionType.name)
        args.putBoolean(ChooseLocationDialogFragment.DELAYED_KEY, delayed)
        val chooseLocationDialogFragment = ChooseLocationDialogFragment()
        chooseLocationDialogFragment.arguments = args
        chooseLocationDialogFragment.show(supportFragmentManager, ChooseLocationDialogFragment.TAG)
    }

    private fun showSearchResultsDialogFragment(resultSet: List<SearchResult>, latitude: Double, longitude: Double) {
        val args = Bundle()
        args.putParcelableArrayList(SearchResultsDialogFragment.RESULT_SET_KEY, ArrayList(resultSet))
        args.putDouble(SearchResultsDialogFragment.LATITUDE_KEY, latitude)
        args.putDouble(SearchResultsDialogFragment.LONGITUDE_KEY, longitude)
        val searchResultsDialogFragment = SearchResultsDialogFragment()
        searchResultsDialogFragment.arguments = args
        searchResultsDialogFragment.show(supportFragmentManager, SearchResultsDialogFragment.TAG)
    }
}

class CitiesAdapter(context: Context) : ArrayAdapter<Location>(context, R.layout.simple_list_layout, CITIES) {
    private val mInflater = LayoutInflater.from(context)
    val icon: Drawable?

    init {
        val tempIcon = ContextCompat.getDrawable(context, R.drawable.ic_action_street_name)
        if (tempIcon != null) {
            icon = DrawableCompat.wrap(tempIcon)
            DrawableCompat.setTint(icon, ContextCompat.getColor(context, R.color.iconColor))
        } else {
            icon = null
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = (convertView
                ?: mInflater?.inflate(R.layout.simple_list_layout, parent, false)) as TextView
        view.text = getItem(position).name
        view.compoundDrawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8f, context.resources.displayMetrics).toInt()
        view.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        return view
    }
}

class ChooseLocationDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ChooseLocationDialogFragment"
        const val API_ACTION_CODE_KEY = "api_action_code_key"
        const val TITLE_KEY = "title_key"
        const val DELAYED_KEY = "delayed_key"
    }

    private var apiActionType: MainActivity.ApiActionType = MainActivity.ApiActionType.UNDEFINED
    private var title: String = ""
    private var delayed: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        if (arguments != null) {
            apiActionType = MainActivity.ApiActionType.valueOf(arguments.getString(API_ACTION_CODE_KEY, MainActivity.ApiActionType.UNDEFINED.name))
            title = arguments.getString(TITLE_KEY, "")
            delayed = arguments.getBoolean(DELAYED_KEY, true)
        }

        val context = requireActivity()
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getTitle())
                .setAdapter(CitiesAdapter(context)) { _, i ->
                    locationSelectedCallback(CITIES[i])
                }
                .setNegativeButton("Cancel", null)
        return builder.create()
    }

    private fun locationSelectedCallback(location: Location) {
        val activity = activity as MainActivity?
        activity?.execApiAction(apiActionType, delayed, location)
    }

    private fun getTitle() = title
}

class SearchResultsAdapter(context: Context, resultSet: List<SearchResult>, private var origLat: Double, private var origLon: Double) : ArrayAdapter<SearchResult>(context, R.layout.title_desc_list_layout, resultSet) {
    private val mInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = (convertView
                ?: mInflater?.inflate(R.layout.title_desc_list_layout, parent, false)) as LinearLayout

        val item = getItem(position)
        val distance = Utils.getDistance(origLat, origLon,item.latitude,item.longitude)

        view.title.text = item.localName
        view.description.text = item.localTypeName
        view.info.text = Utils.getFormattedDistance(distance)
        return view
    }
}

class SearchResultsDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "SearchResultsDialogFragment"
        const val RESULT_SET_KEY = "result_set_key"
        const val LATITUDE_KEY = "latitude_key"
        const val LONGITUDE_KEY = "longitude_key"
    }

    private var resultSet: List<SearchResult>? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        if (arguments != null) {
            resultSet = arguments.getParcelableArrayList(RESULT_SET_KEY)
            latitude = arguments.getDouble(LATITUDE_KEY)
            longitude = arguments.getDouble(LONGITUDE_KEY)
        }

        val context = requireActivity()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Search results - ${resultSet?.size}")
                .setNegativeButton("Cancel", null)

        if (resultSet != null) {
            builder.setAdapter(SearchResultsAdapter(context, resultSet!!, latitude, longitude)) { _, i ->
                val item = resultSet!![i]
                val activity = activity as MainActivity?
                activity?.execApiAction(MainActivity.ApiActionType.INTENT_SHOW_LOCATION, false,
                        Location(item.localName, item.latitude, item.longitude, item.latitude, item.longitude))
            }
        }
        return builder.create()
    }
}

class OsmAndMissingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.dialog_install_osm_and)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Install") { _, _ ->
                    val intent = Intent()
                    intent.data = Uri.parse("market://details?id=net.osmand.plus")
                    startActivity(intent)
                }
        return builder.create()
    }
}

class OsmAndInfoDialog : DialogFragment() {
    companion object {
        const val INFO_KEY = "info_key"
    }

    @Suppress("deprecation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(INFO_KEY)
        val builder = AlertDialog.Builder(requireContext())
        if (message != null) {
            if (Build.VERSION.SDK_INT < 24) {
                builder.setMessage(Html.fromHtml(message))
            } else {
                builder.setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
            }
        }
        builder.setTitle("OsmAnd info:")
        builder.setPositiveButton("OK", null)
        return builder.create()
    }
}

class OpenGpxDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "OpenGpxDialogFragment"
        const val SEND_AS_RAW_DATA_REQUEST_CODE_KEY = "send_as_raw_data_request_code_key"
        const val SEND_AS_URI_REQUEST_CODE_KEY = "send_as_uri_request_code_key"
    }

    private var sendAsRawDataRequestCode: Int? = null
    private var sendAsUriDataRequestCode: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        sendAsRawDataRequestCode = arguments?.getInt(SEND_AS_RAW_DATA_REQUEST_CODE_KEY)
        sendAsUriDataRequestCode = arguments?.getInt(SEND_AS_URI_REQUEST_CODE_KEY)

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Send GPX to OsmAnd as raw data or as URI")
        builder.setNeutralButton("As raw data") { _, _ -> sendAsRawData() }
        builder.setPositiveButton("As URI") { _, _ -> sendAsUri() }
        return builder.create()
    }

    private fun sendAsRawData() {
        val sendAsRawDataRequestCode = sendAsUriDataRequestCode
        if (sendAsRawDataRequestCode != null) {
            requestChooseGpx(sendAsRawDataRequestCode)
        }
    }

    private fun sendAsUri() {
        val sendAsUriDataRequestCode = sendAsUriDataRequestCode
        if (sendAsUriDataRequestCode != null) {
            requestChooseGpx(sendAsUriDataRequestCode)
        }
    }

    private fun requestChooseGpx(requestCode: Int) {
        var intent: Intent
        if (Build.VERSION.SDK_INT < 19) {
            intent = Intent(Intent.ACTION_GET_CONTENT)
        } else {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
        }
        intent.type = "*/*"
        intent = Intent.createChooser(intent, "Choose a file")

        val activity = activity as MainActivity?
        val osmandHelper = activity?.mOsmAndHelper
        if (osmandHelper != null && osmandHelper.isIntentSafe(intent)) {
            startActivityForResult(intent, requestCode)
        } else {
            Toast.makeText(activity, "You need an app capable of selecting files like ES Explorer", Toast.LENGTH_LONG).show()
        }
    }
}

class CloseAfterCommandDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "CloseAfterCommandDialogFragment"
        const val ACTION_CODE_KEY = "action_code_key"
    }

    private var actionType: ActionType = ActionType.UNDEFINED

    enum class ActionType {
        UNDEFINED,
        START_GPX_REC,
        STOP_GPX_REC,
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        if (arguments != null) {
            actionType = ActionType.valueOf(arguments.getString(ACTION_CODE_KEY, ActionType.UNDEFINED.name))
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Close OsmAnd immediately after execution of command?")
        builder.setNeutralButton("Close") { _, _ -> shouldClose(true) }
        builder.setPositiveButton("Don't close") { _, _ -> shouldClose(false) }
        return builder.create()
    }

    private fun shouldClose(close: Boolean) {
        val activity = activity as MainActivity?
        val osmandHelper = activity?.mOsmAndHelper
        if (osmandHelper != null) {
            when (actionType) {
                ActionType.UNDEFINED -> Unit
                ActionType.START_GPX_REC -> osmandHelper.startGpxRec(close)
                ActionType.STOP_GPX_REC -> osmandHelper.stopGpxRec(close)
            }
        }
    }
}