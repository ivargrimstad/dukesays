package dukes.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_user_id", unique = true)
    private Long tmdbUserId;

    private String username;

    @Column(length = 1000)
    private String favoriteGenres;

    @Column(length = 1000)
    private String topDirectors;

    @Column(length = 2000)
    private String topRatedFilms;

    private double averageRating;

    @Column(length = 500)
    private String preferredDecades;

    @Column(length = 1000)
    private String themePreferences;

    @Column(length = 500)
    private String moodProfile;

    private LocalDateTime lastSyncedAt;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMovie> movies = new ArrayList<>();

    public UserProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTmdbUserId() { return tmdbUserId; }
    public void setTmdbUserId(Long tmdbUserId) { this.tmdbUserId = tmdbUserId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public String getTopDirectors() { return topDirectors; }
    public void setTopDirectors(String topDirectors) { this.topDirectors = topDirectors; }

    public String getTopRatedFilms() { return topRatedFilms; }
    public void setTopRatedFilms(String topRatedFilms) { this.topRatedFilms = topRatedFilms; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public String getPreferredDecades() { return preferredDecades; }
    public void setPreferredDecades(String preferredDecades) { this.preferredDecades = preferredDecades; }

    public String getThemePreferences() { return themePreferences; }
    public void setThemePreferences(String themePreferences) { this.themePreferences = themePreferences; }

    public String getMoodProfile() { return moodProfile; }
    public void setMoodProfile(String moodProfile) { this.moodProfile = moodProfile; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public List<UserMovie> getMovies() { return movies; }
    public void setMovies(List<UserMovie> movies) { this.movies = movies; }
}
