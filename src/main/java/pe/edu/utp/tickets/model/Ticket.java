package pe.edu.utp.tickets.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "tickets")
public class Ticket extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false, updatable = false)
    private User creator;

    // Bloqueo optimista: Hibernate suma 1 en cada UPDATE y rechaza el cambio si el número no coincide
    @Version
    @Column(nullable = false)
    private Long version;

    protected Ticket() {
    }

    // Todo ticket nace OPEN: el cliente no decide eso
    public Ticket(String title, String description, Priority priority, Category category, User creator) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.creator = creator;
        this.status = TicketStatus.OPEN;
    }

    // En la Fase 2 aquí vivirán las reglas (por ejemplo, no reabrir un ticket cerrado)
    public void changeStatus(TicketStatus newStatus) {
        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public Category getCategory() {
        return category;
    }

    public User getCreator() {
        return creator;
    }

    public Long getVersion() {
        return version;
    }
}