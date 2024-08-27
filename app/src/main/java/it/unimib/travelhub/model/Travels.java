package it.unimib.travelhub.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
public class Travels implements Serializable, Comparable<Travels> {
    //@SerializedName("publishedAt")
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private List<TravelSegment> destinations;
    private List<TravelMember> members;

    private Date startDate;

    private Date endDate;

    public Travels() {}

    public Travels(long id, String title, String description, Date startDate, Date endDate,
                   List<TravelMember> members, List<TravelSegment> destinations) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinations = destinations;
        this.members = members;
    }
    public List<TravelSegment> getDestinations() {
        return destinations;
    }

    public List<TravelMember> getMembers() {
        return members;
    }

    public void setDestinations(List<TravelSegment> destinations) {
        this.destinations = destinations;
    }

    public void setMembers(List<TravelMember> members) {
        this.members = members;
    }

    public long getId() {return id;}

    public void setId(long id) {this.id = id;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public Date getStartDate() {return startDate;}

    public void setStartDate(Date startDate) {this.startDate = startDate;}

    public Date getEndDate() {return endDate;}

    public void setEndDate(Date endDate) {this.endDate = endDate;}

    public int compareTo(Travels travels) {
        if (this.startDate.compareTo(travels.startDate) == 0)
            return this.endDate.compareTo(travels.endDate);
        else
            return this.startDate.compareTo(travels.startDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Travels travels = (Travels) o;
        return id == travels.id && Objects.equals(title, travels.title) &&
                Objects.equals(description, travels.description) &&
                Objects.equals(startDate, travels.startDate) && Objects.equals(endDate, travels.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, startDate, endDate);
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("destinations", destinations);
        map.put("members", members);
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return "Travels{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", destinations=" + destinations +
                ", members=" + members +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

}
