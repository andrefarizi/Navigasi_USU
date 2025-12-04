package com.mycompany.peta_usu.services;

import java.util.List;

/**
 * CRUDService Interface - Abstraksi untuk service layer
 * Implementasi Abstraction & Polymorphism
 * 
 * Mendefinisikan kontrak CRUD operations yang harus diimplementasi
 * oleh semua service classes
 * 
 * @param <T> Type of entity
 * @author PETA_USU Team
 */
public interface CRUDService<T> {
    
    /**
     * Retrieve all active entities
     * @return List of all entities
     */
    List<T> getAll();
    
    /**
     * Retrieve entity by ID
     * @param id Entity ID
     * @return Entity object or null if not found
     */
    T getById(int id);
    
    /**
     * Insert new entity
     * @param entity Entity to insert
     * @return true if successful, false otherwise
     */
    boolean insert(T entity);
    
    /**
     * Update existing entity
     * @param entity Entity to update
     * @return true if successful, false otherwise
     */
    boolean update(T entity);
    
    /**
     * Delete entity (soft delete recommended)
     * @param id Entity ID to delete
     * @return true if successful, false otherwise
     */
    boolean delete(int id);
    
    /**
     * Search entities by keyword
     * @param keyword Search keyword
     * @return List of matching entities
     */
    List<T> search(String keyword);
    
    /**
     * Count total active entities
     * @return Total count
     */
    int count();
}
