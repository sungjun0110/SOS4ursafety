package sungjun.sos4ursafety.ui.more;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sos4ursafety.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MoreFragment extends Fragment {

    private static final String FILE_NAME = "contacts.txt";
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View moreFragment = inflater.inflate(R.layout.fragment_more, container, false);
        return moreFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button eraseBtn = view.findViewById(R.id.eraseBtn);


        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyVibration(30, 200);
                try {
                    delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void delete() throws IOException {
        FileOutputStream fos = null;

        try {
            fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write("".getBytes());
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

    private void keyVibration(int time, int amp) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(time, amp));
        } else {
            v.vibrate(time);
        }
    }
}
