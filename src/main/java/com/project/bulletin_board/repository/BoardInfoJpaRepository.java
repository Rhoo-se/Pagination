package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.BoardInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardInfoJpaRepository extends JpaRepository<BoardInfo, Long> {

}
