package com.recursivepenguin.bluetoothmanagerforglass;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.widget.CardScrollView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PairDevicesActivity extends Activity implements AdapterView.OnItemClickListener {

    BluetoothAdapter mBluetoothAdapter;
    CardScrollView mCardScrollView;
    BluetoothDeviceCardScrollAdapter adapter;
    BluetoothDevice mSelectedDevice;
    List<BluetoothDevice> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter.startDiscovery();

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.activate();
        mCardScrollView.setOnItemClickListener(this);
        setContentView(mCardScrollView);

        mDevices = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pair, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.pair:
                if (mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();
                pairDevice(mSelectedDevice);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevices.add(device);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new BluetoothDeviceCardScrollAdapter(PairDevicesActivity.this, mDevices);
                        mCardScrollView.setAdapter(adapter);
                    }
                });
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Util.playSound(this, R.raw.sound_tap);
        mSelectedDevice = adapter.getItem(position);
        openOptionsMenu();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {

        }
    }
}
