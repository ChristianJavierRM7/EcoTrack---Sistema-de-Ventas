package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Perdidas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

public class PerdidasJpaController implements Serializable {

    public PerdidasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Perdidas perdidas) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(perdidas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Perdidas perdidas) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            perdidas = em.merge(perdidas);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = perdidas.getId();
                if (findPerdidas(id) == null) {
                    throw new NonexistentEntityException("The perdidas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Perdidas perdidas;
            try {
                perdidas = em.getReference(Perdidas.class, id);
                perdidas.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The perdidas with id " + id + " no longer exists.", enfe);
            }
            em.remove(perdidas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Perdidas> findPerdidasEntities() {
        return findPerdidasEntities(true, -1, -1);
    }

    public List<Perdidas> findPerdidasEntities(int maxResults, int firstResult) {
        return findPerdidasEntities(false, maxResults, firstResult);
    }

    private List<Perdidas> findPerdidasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Perdidas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Perdidas findPerdidas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Perdidas.class, id);
        } finally {
            em.close();
        }
    }

    public int getPerdidasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Perdidas> rt = cq.from(Perdidas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
