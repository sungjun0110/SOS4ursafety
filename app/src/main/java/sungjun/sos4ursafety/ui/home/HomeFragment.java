package sungjun.sos4ursafety.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos4ursafety.MainActivity;
import com.example.sos4ursafety.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    //-------------- VARS
    private static final String FILE_NAME = "contacts.txt";
    private static final int RESULT_PICK_CONTACT = 1;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mPhoneNo = new ArrayList<>();
    private HashMap<String, String> contacts_info = new HashMap<>(); // key = phone no. value = name, check_value
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private Geocoder geocoder;
    private List<Address> addresses;
    private LocationManager locationManager;
    private double latitude, longitude;
    private String fulladdress;
    TextView instructionTextView;
    //--------------- VARS

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View homeFragmentLayout = inflater.inflate(R.layout.fragment_home, container, false);

        return homeFragmentLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ---------- VARS
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build();
        final SoundPool sirenSound = new SoundPool.Builder().setAudioAttributes(attributes).build();
        final int soundId = sirenSound.load(getContext(), R.raw.police_siren, 1);
        final int[] sirenID = new int[1];
        final ToggleButton SOSBtn = view.findViewById(R.id.SOSBtn);
        final ToggleButton flashBtn = view.findViewById(R.id.FlashBtn);
        final ToggleButton flashPowerBtn = view.findViewById(R.id.flashPowerBtn);
        final ToggleButton morseBtn = view.findViewById(R.id.MorseBtn);
        final Button addBtn = view.findViewById(R.id.addBtn);
        instructionTextView = view.findViewById(R.id.instructionTextView);

        // ----------- VARS
        load();
        morseBtn.setSelected(true);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        //-----------recycler view init
        buildRecyclerView();
        //-----------recycler view init

        if (mPhoneNo.size() > 0) {
            instructionTextView.setText("");
        }

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(20, 200);
                Intent in = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult (in, RESULT_PICK_CONTACT);
            }
        });

        SOSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyVibration(50, -1);
                if (!SOSBtn.isSelected()) {
                    sirenID[0] = sirenSound.play(soundId, 1, 1, 1, -1, 1);
                    SOSBtn.setSelected(true);
                }
                else {
                    SOSBtn.setSelected(false);
                    sirenSound.stop(sirenID[0]);
                }
            }
        });

        view.findViewById(R.id.SendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(50, -1);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    getLocation();
                }
                for (int i = 0; i < mPhoneNo.size(); i++){
                    sendSMS(mPhoneNo.get(i));
                }
            }
        });

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(30, 200);
                if (!flashBtn.isSelected()) {
                    if(flashPowerBtn.isSelected()) {
                        getActivity().stopService(new Intent(getActivity(), SosService.class));

                        flashPowerBtn.setSelected(false);
                    }
                    morseBtn.setSelected(false);
                    flashBtn.setSelected(true);
                }
            }
        });

        morseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(30, 200);
                if (!morseBtn.isSelected()) {
                    if (flashPowerBtn.isSelected()) {
                        try {
                            flashOn(false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        flashPowerBtn.setSelected(false);
                    }
                    flashBtn.setSelected(false);
                    morseBtn.setSelected(true);
                }
            }
        });

        flashPowerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(30, 200);
                if (!flashPowerBtn.isSelected()){
                    flashPowerBtn.setSelected(true);
                    if (flashBtn.isSelected()) {
                        try {
                            flashOn(true);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else if (morseBtn.isSelected()){
                        getActivity().startService(new Intent(getActivity(), SosService.class));
                    }

                }
                else {
                    flashPowerBtn.setSelected(false);
                    if (flashBtn.isSelected()) {
                        try {
                            flashOn(false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else if (morseBtn.isSelected()){
                        getActivity().stopService(new Intent(getActivity(), SosService.class));
                    }
                }
            }
        });

    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        ){
            ((MainActivity)getActivity()).requestLocationPermission();
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (locationGPS != null) {
                latitude = locationGPS.getLatitude();
                longitude = locationGPS.getLongitude();
            } else if (locationNetwork != null) {
                latitude = locationNetwork.getLatitude();
                longitude = locationNetwork.getLongitude();

            } else if (locationPassive != null) {
                latitude = locationPassive.getLatitude();
                longitude = locationPassive.getLongitude();

            } else {
                Toast.makeText(getContext(), "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                fulladdress = addresses.get(0).getAddressLine(0);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_PICK_CONTACT) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = getContext().getContentResolver().query(contactData, null, null, null, null);

                if (c.moveToFirst()) {
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String number = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    number = number.replace("(","").replace(")","").replace("-","").replace(" ","");

                    if (contacts_info.containsKey(number)){
                        Toast.makeText(getContext(), "The contact you selected already exists.", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        contacts_info.put(number, name);
                        mPhoneNo.add(number);
                        mNames.add(name);
                    }

                    Toast.makeText(getContext(), "You've picked: " + name, Toast.LENGTH_LONG).show();

                    adapter.notifyItemInserted(mPhoneNo.size());

                    try {
                        save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (mPhoneNo.size() > 0) {
                        instructionTextView.setText("");
                    }
                }
            }
        }
    }

    public void buildRecyclerView() {
        recyclerView = getView().findViewById(R.id.contactsView);
        adapter = new RecyclerViewAdapter(getActivity(), mNames, mPhoneNo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onSendClick(int position) {
                keyVibration(30, 200);
                sendSMSatPosition(position);
            }

            @Override
            public void onDeleteClick(int position) throws IOException {
                keyVibration(30, 200);
                deleteItem(position);
                save();
                adapter.notifyDataSetChanged();
                if (mPhoneNo.size() == 0) {
                    instructionTextView.setText("Press ADD button below \nto add contacts");
                }
            }
        });
    }

    public void sendSMSatPosition(int position) {
        getLocation();
        sendSMS(mPhoneNo.get(position));
    }

    public void deleteItem(int position) {
        contacts_info.remove(mPhoneNo.get(position));
        mPhoneNo.remove(position);
        mNames.remove(position);
    }

    private void keyVibration(int time, int amp) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(time, amp));
        } else {
            v.vibrate(time);
        }
    }

    private void sendSMS (String phoneNo) {
        getLocation();
        String msg;
        if (fulladdress == null && latitude == 0.0 && longitude == 0.0){
            msg = "I need your help!\nLocation is unknown.\nSent via SOS app";
        } else {
            msg = "I need your help! \nAddress: " + fulladdress + "\nLatitude: " + latitude + "\nLongitude: " + longitude + "\nSent via SOS app";
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ((MainActivity)getActivity()).requestSMSPermission();
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                Toast.makeText(getContext(), phoneNo + "\n" + msg, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void flashOn(Boolean mode) throws CameraAccessException {
        CameraManager flash = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        String cameraID = flash.getCameraIdList()[0];
        flash.setTorchMode(cameraID, mode);
    }

    public void save() throws IOException{
        ArrayList<String> text = new ArrayList<>();
        FileOutputStream fos = null;

        for (int i = 0; i < mPhoneNo.size(); i++) {
            text.add(mPhoneNo.get(i) + ", " + mNames.get(i) + "\n");
        }

        try {
            fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            for (int i = 0; i < text.size(); i++) {
                fos.write(text.get(i).getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null){
                try{
                    fos.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }

    public void load() {
        FileInputStream fis = null;

        try {
            fis = getActivity().openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text;
            String[] arrayOfText;

            while ((text = br.readLine()) != null) {
                arrayOfText = text.split(", "); // name, phone number
                mPhoneNo.add(arrayOfText[0]);
                mNames.add(arrayOfText[1]);
            }

            for (int i = 0; i < mPhoneNo.size(); i++) {
                contacts_info.put(mPhoneNo.get(i), mNames.get(i));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
