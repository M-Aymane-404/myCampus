package com.mycampus.backend.repositorys;

import com.mycampus.backend.Entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IClientRepository extends JpaRepository<Client, Long> {

}
