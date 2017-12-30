package com.edu.uz.sledz.bartosz.obd2;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.edu.uz.sledz.bartosz.obd2.databinding.ActivityMainBinding;

import java.lang.reflect.Method;
import java.util.ArrayList;

public final class MainActivity extends AppCompatActivity {

    private final IntentFilter filter = new IntentFilter();
    private ProgressDialog dialog;
    private ActivityMainBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setLang();
        setTitle(getString(R.string.app_name));

        checkDeviceSupportBluetooth();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Log.e("Already unregistered", e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            if (isBluetoothEnabled()) {
                enableBluetooth();
                setOn();
                setPairedDevicesList();
            } else {
                disableBluetooth();
                setOff();
            }
        }
    }

    private void initComponents() {
        binding.btnTurnOnOffBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isBluetoothEnabled()) {
                    disableBluetooth();
                    setOff();
                } else {
                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setTitle(getString(R.string.turningOn));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.show();
                    enableBluetooth();
                    registerReceiver(receiver, filter);
                }
            }
        });

        binding.btnFindDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                bluetoothAdapter.startDiscovery();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle(getString(R.string.scanning));
                dialog.setIcon(android.R.drawable.ic_search_category_default);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                                bluetoothAdapter.cancelDiscovery();
                            }
                        });
                registerReceiver(receiver, filter);
            }
        });

        binding.about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showAboutInfo();
            }
        });
    }

    /**
     * Listener for bluetooth actions.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                        setOn();
                        dialog.dismiss();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    devices = new ArrayList<>();
                    dialog.show();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    bluetoothAdapter.cancelDiscovery();
                    dialog.dismiss();
                    showAvailableDevices();
                    //Toast.makeText(MainActivity.this, devices.toString(), Toast.LENGTH_LONG).show();
                    // Intent newIntent = new Intent(MainActivity.this, DevicesBlaBla.class);
                    // newIntent.putParcelableArrayListExtra("device.list", devices);
                    // startActivity(newIntent);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    Toast.makeText(MainActivity.this, device.getName(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void showAvailableDevices() {
        if (!devices.isEmpty()) {
            final ArrayList<String> deviceList = new ArrayList<>();
            for (final BluetoothDevice device : devices) {
                deviceList.add(String.format("%s (%s)", device.getName(), device.getAddress()));
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            final AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(R.string.selectDevice)
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_search_category_default)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            final String selectedDeviceName = adapter.getItem(which);
                            for (BluetoothDevice device : devices) {
                                final String deviceName = String.format("%s (%s)", device.getName(), device.getAddress());
                                if (deviceName.equals(selectedDeviceName)) {
                                    pairDevice(device);
                                }
                            }
                            Toast.makeText(MainActivity.this, selectedDeviceName, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(MainActivity.this, R.string.notFound, Toast.LENGTH_LONG).show();
        }
    }

    private void setPairedDevicesList() {
       /* final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        final View listView = getLayoutInflater().inflate(R.layout.listview, null);
        builder.setView(listView)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_search_category_default)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        ListView lv = (ListView) listView.findViewById(R.id.listView);
        final AlertDialog alert = builder.create();
        alert.setTitle(R.string.selectDevice);
        lv.setAdapter(new MyListAdapter(MainActivity.this, R.layout.activity_device_list, deviceList));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Toast.makeText(MainActivity.this, "You have selected -: " + deviceList.get(position),
                        Toast.LENGTH_SHORT).show();
                alert.cancel();
            }
        });
        alert.show();
    } else {
        Toast.makeText(MainActivity.this, R.string.notFound, Toast.LENGTH_LONG).show();
    }*/

    }


    private void checkDeviceSupportBluetooth() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(R.string.bluetoothErrorTitle)
                    .setMessage(R.string.bluetoothError)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            initComponents();
        }
    }

    private void showAboutInfo() {
        final View dialogView = getLayoutInflater().inflate(R.layout.activity_about, null);
        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.about)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void pairDevice(final BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(final BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOn() {
        binding.bluetoothStatus.setText(R.string.bluetoothOn);
        binding.bluetoothStatus.setTextColor(Color.GREEN);
        binding.btnTurnOnOffBluetooth.setText(R.string.turnOffBluetooth);
        binding.btnFindDevice.setEnabled(true);
    }

    private void setOff() {
        binding.bluetoothStatus.setText(R.string.bluetoothOff);
        binding.bluetoothStatus.setTextColor(Color.RED);
        binding.btnTurnOnOffBluetooth.setText(R.string.turnOnBluetooth);
        binding.btnFindDevice.setEnabled(false);
    }

    private boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    private void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    private void disableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
    }
}
