package org.eflerrr.client.repository;

import org.eflerrr.client.model.entity.ChatEntity;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, String> {

    Optional<ChatEntity> findByChatNameAndEncryptionAlgorithm(
            String chatName, EncryptionAlgorithm encryptionAlgorithm);

}
