package com.spring.diary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.diary.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

	List<FileEntity> findAllByDiaryNo(@Param("diaryNo") Long diaryNo);

}
