package com.example.ridekeeper;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TestFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view =  inflater.inflate(R.layout.fragment_test, container, false);

		Button test = (Button) view.findViewById(R.id.button_test);
		test.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "Clicked!!", Toast.LENGTH_SHORT).show();
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Vehicle");
				query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
				
				query.findInBackground(new FindCallback<ParseObject>() {
					
					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						Toast.makeText(getActivity(), objects.get(0).getObjectId(), Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
		
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}
}
