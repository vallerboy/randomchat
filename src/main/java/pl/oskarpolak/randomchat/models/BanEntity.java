package pl.oskarpolak.randomchat.models;


import lombok.Data;

import javax.persistence.*;
import java.lang.annotation.Target;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ban")
public class BanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String ip;
    private LocalDateTime date;
}
