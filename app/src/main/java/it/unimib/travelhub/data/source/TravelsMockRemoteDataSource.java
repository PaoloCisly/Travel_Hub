package it.unimib.travelhub.data.source;

import static it.unimib.travelhub.util.Constants.TRAVELS_TEST_JSON_FILE;

import java.io.IOException;

import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;
import it.unimib.travelhub.util.JSONParserUtil;

public class TravelsMockRemoteDataSource extends BaseTravelsRemoteDataSource{
    private final JSONParserUtil jsonParserUtil;

    public TravelsMockRemoteDataSource(JSONParserUtil jsonParserUtil) {
        this.jsonParserUtil = jsonParserUtil;
    }

    public void getAllUserTravel() {
        TravelsResponse travelsResponse = null;

        try {
            travelsResponse = jsonParserUtil.parseJSONFileWithGSon(TRAVELS_TEST_JSON_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (travelsResponse != null) {
            travelsCallback.onSuccessFromRemote(travelsResponse, System.currentTimeMillis());
        } else {
            travelsCallback.onFailureFromRemote(new Exception("Error parsing file"));
        }
    }

    @Override
    public void addTravel(Travels travel) {

    }

    @Override
    public void updateTravel(Travels newTravel, Travels oldTravel) {

    }


    @Override
    public void deleteTravel(Travels travels) {

    }
}
