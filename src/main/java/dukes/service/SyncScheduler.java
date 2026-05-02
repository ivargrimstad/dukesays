package dukes.service;

import dukes.entity.UserProfile;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class SyncScheduler {

    private static final Logger LOG = Logger.getLogger(SyncScheduler.class.getName());

    @PersistenceContext(unitName = "knowme")
    private EntityManager em;

    @Inject
    private TmdbService tmdbService;

    @Inject
    private ProfileBuilder profileBuilder;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void syncAllProfiles() {
        if (tmdbService.getSessionId() == null) {
            return;
        }

        List<UserProfile> profiles = em.createQuery(
                "SELECT p FROM UserProfile p", UserProfile.class)
                .getResultList();

        for (UserProfile profile : profiles) {
            try {
                var rated = tmdbService.getRatedMovies(profile.getTmdbUserId());
                var favorites = tmdbService.getFavoriteMovies(profile.getTmdbUserId());
                var watchlist = tmdbService.getWatchlistMovies(profile.getTmdbUserId());

                profileBuilder.syncFromTmdb(
                        profile.getTmdbUserId(),
                        profile.getUsername(),
                        rated, favorites, watchlist);

                LOG.info("Auto-synced profile for user: " + profile.getUsername());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to auto-sync profile for user: " + profile.getUsername(), e);
            }
        }
    }
}