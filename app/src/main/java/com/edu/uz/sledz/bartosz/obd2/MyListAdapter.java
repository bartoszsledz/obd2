package com.edu.uz.sledz.bartosz.obd2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

class MyListAdapter extends ArrayAdapter<String> {

    private LayoutInflater inflater;
    private Context context;
    private List<String> deviceList;

    public MyListAdapter(final Context context, final int resource, final List<String> deviceList) {
        super(context, resource, deviceList);
        this.context = context;
        this.deviceList = deviceList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull final ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.activity_device_list, null);
            holder.setName((TextView) view.findViewById(R.id.name));
            holder.setAddress((TextView) view.findViewById(R.id.address));
            holder.setBtnPairUnpair((Button) view.findViewById(R.id.btnPairUnpair));
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // BluetoothDevice device = pairedDevices.get(position);

        //holder.getName().setText(device.getName());
        //holder.getAddress().setText(device.getAddress());
        //holder.getBtnPairUnpair().setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? R.string.unPair : R.string.pair);
        holder.getBtnPairUnpair().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                /*if (mListener != null) {
                    mListener.onPairButtonClick(position);
                }*/
            }
        });

        holder.getName().setText(deviceList.get(position));
        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private Button btnPairUnpair;

        public void setName(final TextView name) {
            this.name = name;
        }

        public void setAddress(final TextView address) {
            this.address = address;
        }

        public void setBtnPairUnpair(final Button btnPairUnpair) {
            this.btnPairUnpair = btnPairUnpair;
        }

        public TextView getName() {
            return name;
        }

        public TextView getAddress() {
            return address;
        }

        public Button getBtnPairUnpair() {
            return btnPairUnpair;
        }
    }
}