package engine;

import model.ILatentEntity;
import java.util.List;

/**
 * interface that defines the contract for loading latent entities into the system.
 * Any data source (JSON, CSV, Database) must implement this interface.
 */
public interface IDataLoader {
    List<ILatentEntity> loadEntities(String fullVectorsPath, String pcaVectorsPath) throws Exception; //Loads entities from the specified paths.
}