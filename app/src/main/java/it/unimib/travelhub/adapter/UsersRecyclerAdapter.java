package it.unimib.travelhub.adapter;

import static it.unimib.travelhub.util.Constants.PICS_FOLDER;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import it.unimib.travelhub.GlobalClass;
import it.unimib.travelhub.R;
import it.unimib.travelhub.data.repository.user.IUserRepository;
import it.unimib.travelhub.data.source.RemoteFileStorageSource;
import it.unimib.travelhub.data.user.UserRemoteFirestoreDataSource;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.ui.travels.AddTravelActivity;
import it.unimib.travelhub.ui.travels.TravelActivity;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {
    List<TravelMember> data;
    private final IUserRepository userRepository;
    int type;
    private final String TAG = UsersRecyclerAdapter.class.getSimpleName();
    Activity activity;

    String color = null;

    public interface OnLongButtonClickListener {
        void onLongButtonItemClick(TravelMember travelMember, ImageView seg_long_button);
    }
    private static OnLongButtonClickListener onLongButtonClickListener = null;

    public UsersRecyclerAdapter(List<TravelMember> data, int type, Activity activity, OnLongButtonClickListener onLongButtonClickListener, IUserRepository userRepository) {
        UsersRecyclerAdapter.onLongButtonClickListener = onLongButtonClickListener;
        this.activity = activity;
        this.data = data;
        this.type = type;
        this.userRepository = userRepository;
    }

    public UsersRecyclerAdapter(List<TravelMember> data, int type, Activity activity, OnLongButtonClickListener onLongButtonClickListener, IUserRepository userRepository, String color) {
        UsersRecyclerAdapter.onLongButtonClickListener = onLongButtonClickListener;
        this.activity = activity;
        this.data = data;
        this.type = type;
        this.userRepository = userRepository;
        this.color = color;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (type == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.compact_friend_list_item, parent, false);
        }
        return new ViewHolder(view, type, activity);
    }
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }
    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }
     class ViewHolder extends RecyclerView.ViewHolder {
        TextView participant_name;
        ImageView participant_image;
        ImageView participant_creator;
        int type;

        Activity activity;
        public ViewHolder(@NonNull View itemView, int type, Activity activity) {
            super(itemView);
            this.type = type;
            this.activity = activity;
            participant_name = itemView.findViewById(R.id.participant_name);
            participant_image = itemView.findViewById(R.id.participant_image);
            participant_creator = itemView.findViewById(R.id.participant_creator_flag);
        }
        public void bind(TravelMember travelMember) {
            participant_name.setText(travelMember.getUsername());
            setProfileImage(travelMember, participant_image);

            if (travelMember.getRole() == TravelMember.Role.CREATOR) {
                participant_creator.setVisibility(View.VISIBLE);
            }

            if (color != null) {
                participant_name.setTextColor(android.graphics.Color.parseColor(color));
            }

            if (activity instanceof TravelActivity){
                participant_image.setOnLongClickListener(v -> {
                    if (((TravelActivity) itemView.getContext()).isTravelCreator && ((TravelActivity) itemView.getContext()).enableEdit){
                        onLongButtonClickListener.onLongButtonItemClick(travelMember, participant_image);
                    }
                    return true;
                });
            } else if (activity instanceof AddTravelActivity){
                participant_image.setOnLongClickListener(v -> {
                    onLongButtonClickListener.onLongButtonItemClick(travelMember, participant_image);
                    return true;
                });

            }


        }
    }

    private void setProfileImage(TravelMember travelMember, ImageView participant_image) {
        // download the profile images of the members of the ongoing travel
        userRepository.getUserProfileImage(travelMember.getIdToken(), new UserRemoteFirestoreDataSource.getProfileImagesCallback() {
            @Override
            public void onProfileImagesSuccess(String profileImagesURL) {
                Log.d(TAG, "Profile images URLs: " + profileImagesURL);

                if (profileImagesURL != null) {
                    try {
                        File dir = new File(GlobalClass.getContext().getFilesDir() + PICS_FOLDER);
                        if (!dir.exists())
                            if(!dir.mkdir())
                                Log.e(TAG, "Error creating profile image folder");
                        File file = new File(GlobalClass.getContext().getFilesDir() + PICS_FOLDER + travelMember.getIdToken() + ".webp");
                        if (!file.exists())
                            if(!file.createNewFile())
                                Log.e(TAG, "Error creating profile image file");
                        else {
                            participant_image.setImageURI(Uri.fromFile(file));
                        }
                        userRepository.downloadProfileImage(profileImagesURL, file, new RemoteFileStorageSource.downloadCallback() {
                            @Override
                            public void onSuccessDownload(String url) {
                                Log.d(TAG, "Profile image downloaded successfully");
                                participant_image.setImageURI(Uri.fromFile(file));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Error downloading profile image: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating profile image file: " + e.getMessage());
                    }
                }

            }
            @Override
            public void onProfileImagesFailure(Exception e) {
                Log.e(TAG, "Error getting profile images URLs: " + e.getMessage());
                participant_image.setImageDrawable(AppCompatResources.getDrawable(GlobalClass.getContext(), R.drawable.baseline_person_24));
            }
        });
    }

}