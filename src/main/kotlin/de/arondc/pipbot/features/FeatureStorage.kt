package de.arondc.pipbot.features


import jakarta.annotation.PostConstruct
import jakarta.persistence.*
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeatureStorage : JpaRepository<FeatureEntity, Long> {
    fun findByName(name: String): FeatureEntity?

    @Query("select f.enabled from FeatureEntity f where f.name=:featureName")
    fun isEnabled(@Param("featureName") featureName: String): Boolean
}

@Entity
@Table(name = "features")
class FeatureEntity(
    var name: String,
    var enabled: Boolean,
    @Id
    @SequenceGenerator(
        name = "feature_sequence",
        sequenceName = "FEATURES_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "feature_sequence"
    ) var id: Long? = null
)

@Configuration
class FeatureStorageInitializer(val featureStorage: FeatureStorage) {
    @PostConstruct
    fun init() {
        addMissingFeaturesToDatabase()
        deleteObsoleteFeaturesFromDatabase()
    }

    private fun deleteObsoleteFeaturesFromDatabase() {
        featureStorage
            .findAll()
            .filter { fe -> Feature.entries.none { feature -> feature.name == fe.name } }
            .forEach { featureStorage.delete(it) }
    }

    private fun addMissingFeaturesToDatabase() {
        Feature.entries.forEach { feature ->
            featureStorage.findByName(feature.name)
                ?: featureStorage.save(FeatureEntity(feature.name, true))
        }
    }
}
