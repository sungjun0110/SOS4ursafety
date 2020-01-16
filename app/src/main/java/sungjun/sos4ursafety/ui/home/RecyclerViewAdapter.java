package sungjun.sos4ursafety.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos4ursafety.R;

import java.io.IOException;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> mNames;
    private ArrayList<String> mNumbers;
    private Context mContext;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onSendClick(int position);
        void onDeleteClick(int position) throws IOException;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public RecyclerViewAdapter(Context context, ArrayList<String> names, ArrayList<String> numbers) {
        mNames = names;
        mNumbers = numbers;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(mNames.get(position));
        holder.number.setText(mNumbers.get(position));
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, number;
        private Button smallSendBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            smallSendBtn = itemView.findViewById(R.id.smallSendBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);

            smallSendBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSendClick(position);
                        }
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                listener.onDeleteClick(position);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
}
