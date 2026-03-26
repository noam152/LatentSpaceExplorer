package engine;

import model.ILatentEntity;
import java.util.List;

/**
 * IDataLoader interface defines the contract for loading latent entities into the system.
 * Any data source (JSON, CSV, Database) must implement this interface.
 */
public interface IDataLoader {

    /**
     * Loads entities from the specified paths.
     */
    List<ILatentEntity> loadEntities(String fullVectorsPath, String pcaVectorsPath) throws Exception;
}