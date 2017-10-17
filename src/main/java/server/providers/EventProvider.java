package server.providers;

import server.models.Event;
import server.models.User;
import server.util.DBManager;

import javax.ws.rs.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Filip on 10-10-2017.
 */
public class EventProvider {

    public Event getEvent(int event_id) {
        ArrayList<Event> getEvent = new ArrayList<>();
        Event event = null;
        ResultSet resultSet = null;

        try {
           PreparedStatement getEventStmt = DBManager.getConnection()
                    .prepareStatement("SELECT FROM events WHERE event_id = ?");

           getEventStmt.setInt(1, event_id);

            resultSet = getEventStmt.executeQuery();

            while (resultSet.next()) {
                event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("title"),
                    resultSet.getTimestamp("created"),
                    new User(resultSet.getInt("owner_id")),
                    resultSet.getTimestamp("start"),
                    resultSet.getTimestamp("end"),
                    resultSet.getString("description"));


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return event;
    }
}

