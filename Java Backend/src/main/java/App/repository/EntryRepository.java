package App.repository;

/**
 * Created by milan on 20.11.16.
 */


import model.Entry;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EntryRepository extends MongoRepository<Entry, String> {

    @Bean
    public Entry findByWord(String word);
    @Bean
    public List<Entry> findByTranslation(String translation);
    @Bean
    public List<Entry> findByLanguages(String[] languages);
    @Bean
    public Entry findById(int id);

}
