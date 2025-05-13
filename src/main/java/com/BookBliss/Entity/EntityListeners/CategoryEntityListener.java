package com.BookBliss.Entity.EntityListeners;


import com.BookBliss.Entity.Category;
import com.BookBliss.Utils.SpringContextUtil;
import jakarta.persistence.*;

public class CategoryEntityListener {

    @PostLoad
    public void postLoad(Category category) {
        updateBookCount(category);
    }

    @PostPersist
    @PostUpdate
    public void postUpdate(Category category) {
        updateBookCount(category);
    }

    private void updateBookCount(Category category) {
        if (category.getBooks() != null) {
            EntityManager em = getEntityManager();
            if (em != null) {
                // Use a native query to count books from the join table
                Query query = em.createNativeQuery(
                        "SELECT COUNT(*) FROM book_categories WHERE category_id = :categoryId"
                );
                query.setParameter("categoryId", category.getId());
                Number count = (Number) query.getSingleResult();
                category.setBookCount(count.intValue());

                // Need to merge if this is happening in a separate transaction
                if (em.contains(category)) {
                    em.merge(category);
                    em.flush();
                }
            }
        }
    }

    // Helper method to get EntityManager - Implementation depends on your application setup
    private EntityManager getEntityManager() {
        try {
            // In a Spring Boot application
            return SpringContextUtil.getBean(EntityManager.class);
        } catch (Exception e) {
            return null;
        }
    }
}
