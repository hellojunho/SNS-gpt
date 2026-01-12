package com.sns.gpt.news.repository;

import com.sns.gpt.news.model.NewsFile;
import com.sns.gpt.news.model.NewsSource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsFileRepository extends JpaRepository<NewsFile, Long> {
    List<NewsFile> findTop20BySourceOrderByFetchedAtDesc(NewsSource source);
    List<NewsFile> findTop50ByOrderByFetchedAtDesc();
}
