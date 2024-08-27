package it.unimib.travelhub.ui.travels;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import it.unimib.travelhub.R;
import it.unimib.travelhub.adapter.UsersRecyclerAdapter;
import it.unimib.travelhub.data.repository.user.IUserRepository;
import it.unimib.travelhub.databinding.FragmentTravelDashboardBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.User;
import it.unimib.travelhub.ui.welcome.UserViewModel;
import it.unimib.travelhub.ui.welcome.UserViewModelFactory;
import it.unimib.travelhub.util.ServiceLocator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TravelDashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TravelDashboardFragment extends Fragment {

    private static final String TRAVEL = "travel";
    protected RecyclerView.LayoutManager mLayoutManager;
    private FragmentTravelDashboardBinding binding;
    private Travels travel;
    private UserViewModel userViewModel;
    private IUserRepository userRepository;
    private static final String TAG = TravelDashboardFragment.class.getSimpleName();

    public TravelDashboardFragment(Travels travel) {
        // Required empty public constructor
        this.travel = travel;
    }


    public static TravelDashboardFragment newInstance(Travels travel) {
        TravelDashboardFragment fragment = new TravelDashboardFragment(travel);
        Bundle args = new Bundle();
        args.putSerializable(TRAVEL, travel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userRepository =
                ServiceLocator.getInstance().getUserRepository();

        if (userRepository != null) {
            userViewModel = new ViewModelProvider(
                    requireActivity(),
                    new UserViewModelFactory(userRepository)).get(UserViewModel.class);
        } else {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show();
        }
        if (getArguments() != null) {
            travel = (Travels) getArguments().getSerializable(TRAVEL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTravelDashboardBinding.inflate(inflater, container, false);
        handleParticipants();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (travel.getDescription() == null || travel.getDescription().isEmpty()) {
            binding.menuDescription.setText(R.string.travel_no_description);
        } else {
            binding.travelDescription.setText(travel.getDescription());
        }

        editTravel();

        binding.menuDescription.setOnClickListener(v -> {
            if (binding.travelDescription.getVisibility() == View.GONE) {
                binding.travelDescription.setVisibility(View.VISIBLE);
                binding.menuDescription.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_book_24, 0, R.drawable.baseline_keyboard_arrow_up_24, 0);
            } else {
                binding.travelDescription.setVisibility(View.GONE);
                binding.menuDescription.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_book_24, 0, R.drawable.baseline_keyboard_arrow_down_24, 0);
            }
        });

        long diff = travel.getEndDate().getTime() - travel.getStartDate().getTime();
        long diffToday = System.currentTimeMillis() - travel.getStartDate().getTime();
        diffToday = diffToday < 0 ? 0 : diffToday;

        int progress = (int) (diffToday * 100 / diff);

        binding.travelDuration.setText(String.valueOf(diff / (1000 * 60 * 60 * 24)));
        binding.travelStart.setText(travel.getDestinations().get(0).getLocation());
        binding.travelDestinations.setText(String.valueOf((travel.getDestinations().size() - 1)));
        binding.travelParticipants.setText(String.valueOf(travel.getMembers().size()));
        binding.progressBar.setProgress(progress);


    }

    private void editTravel() {
        TravelActivity travelActivity = (TravelActivity) requireActivity();
        TextInputEditText travelDescription = binding.travelDescription;
        travelDescription.setOnClickListener(v -> {
            if (travelActivity.enableEdit){
                travelDescription.setFocusableInTouchMode(true);
                travelDescription.setFocusable(true);
                travelDescription.requestFocus();
                travelDescription.setOnFocusChangeListener((v1, hasFocus) -> {
                    if (!hasFocus) {
                        travelDescription.setFocusable(false);
                    }
                });
            }

        });
        travelDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                travel.setDescription(s.toString());
                travelActivity.showEditButton();
            }
            @Override
            public void afterTextChanged(Editable s) {
                travelDescription.setFocusable(false);
            }
        });
    }

    private void handleParticipants(){
        RecyclerView recyclerView = binding.friendsRecyclerView;
        mLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        ArrayList<TravelMember> dataSource = new ArrayList<>(travel.getMembers());
        UsersRecyclerAdapter usersRecyclerAdapter = new UsersRecyclerAdapter(dataSource, 2, requireActivity(),
                (travelMember, seg_long_button) -> {
                    if (getContext() == null) {
                        Log.e(TAG, "Context is null");
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(getContext(), seg_long_button);
                    popupMenu.getMenuInflater().inflate(R.menu.edit_travel_segment, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        // Toast message on menu item clicked
                        if (item.getItemId() == R.id.delete_segment) {
                            remove_participant(travelMember);
                        }
                        return true;
                    });
                    popupMenu.show();
                }, userRepository);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(usersRecyclerAdapter);
        if (((TravelActivity) requireActivity()).isTravelCreator){
            binding.layoutAddParticipant.setVisibility(View.VISIBLE);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            binding.buttonAddParticipant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    @SuppressLint("InflateParams") View view1 = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout_add_participant, null);
                    bottomSheetDialog.setContentView(view1);
                    bottomSheetDialog.show();

                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button add_participant = view1.findViewById(R.id.button_add_partecipant);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextInputLayout username = view1.findViewById(R.id.username_text_field);

                    add_participant.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (Objects.requireNonNull(username.getEditText()).getText() == null || username.getEditText().getText().toString().isEmpty()) {
                                username.setError(getString(R.string.error_empty_username));
                            }else{
                                username.setError(null);
                                userViewModel.isUsernameAlreadyTaken(username.getEditText().getText().toString());

                                Observer<Result> observer = new Observer<Result>(){
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onChanged(Result result) {
                                        if (result instanceof Result.Error) {
                                            Log.d(TAG, ((Result.Error) result).getMessage());
                                            username.setError(((Result.Error) result).getMessage());
                                        } else {
                                            User user = ((Result.UserResponseSuccess) result).getData();
                                            Log.d(TAG, "User found: " + user);
                                            if (user != null) {
                                                travel.getMembers().add(new TravelMember(user.getUsername(), user.getIdToken(), TravelMember.Role.MEMBER));
                                                dataSource.add(new TravelMember(user.getUsername(), user.getIdToken(), TravelMember.Role.MEMBER));
                                                usersRecyclerAdapter.notifyDataSetChanged();
                                                TravelActivity travelActivity = (TravelActivity) requireActivity();
                                                travelActivity.showEditButton();
                                                bottomSheetDialog.dismiss();
                                            } else {
                                                username.setError(getString(R.string.error_empty_username));
                                            }
                                        }
                                        userViewModel.getIsUsernameAlreadyTaken().removeObserver(this);
                                    }
                                };

                                userViewModel.getIsUsernameAlreadyTaken().observe(getViewLifecycleOwner(), observer);

                            }
                        }

                    });
                }


            });
        }
    }

    private void remove_participant(TravelMember travelMember) {
        if (travelMember.getRole() == TravelMember.Role.CREATOR) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.error_delete_participant), Snackbar.LENGTH_SHORT).show();
            return;
        }
        travel.getMembers().remove(travelMember);
        TravelActivity travelActivity = (TravelActivity) requireActivity();
        travelActivity.updateTravel(travel);
    }


}