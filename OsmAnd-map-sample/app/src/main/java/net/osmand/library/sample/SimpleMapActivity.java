package net.osmand.library.sample;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.activities.RestartActivity;
import net.osmand.plus.views.MapViewWithLayers;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.data.LatLon;
import java.util.List; // Java List class
import java.util.ArrayList;
public class SimpleMapActivity extends AppCompatActivity {

	private OsmandApplication app;
	private OsmandMapTileView mapTileView;
	private MapViewWithLayers mapViewWithLayers;

	private List<LatLon> getBoundaryPoints() {
		List<LatLon> points = new ArrayList<>();
		points.add(new LatLon(40.86523, 14.22554)); // Top-left
		points.add(new LatLon(40.86523, 14.22201)); // Top-right
		points.add(new LatLon(40.86382, 14.22201)); // Bottom-right
		points.add(new LatLon(40.86382, 14.22554)); // Bottom-left
		points.add(new LatLon(40.86523, 14.22554)); // Closing the polygon
		return points;
	}
	private boolean isWithinBoundary(LatLon location) {
		double lat = location.getLatitude();
		double lon = location.getLongitude();

		return lat <= 40.86523 && lat >= 40.86382 && lon >= 14.22201 && lon <= 14.22554;
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_map_activity);
		mapViewWithLayers = findViewById(R.id.map_view_with_layers);

		app = (OsmandApplication) getApplication();

		mapTileView = app.getOsmandMap().getMapView();
		mapTileView.setupRenderingView();
		LatLon userLocation = new LatLon(40.86423, 14.22401);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle("Simple map");
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}

		CompoundButton openglSwitch = findViewById(R.id.opengl_switch);
		openglSwitch.setChecked(app.getSettings().USE_OPENGL_RENDER.get());
		openglSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			app.getSettings().USE_OPENGL_RENDER.set(isChecked);
			RestartActivity.doRestart(this);
		});

		//set start location and zoom for map
		mapTileView.setIntZoom(14);
		mapTileView.setLatLon(40.86483, 14.22154);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapViewWithLayers.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapViewWithLayers.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapViewWithLayers.onDestroy();
	}
}
