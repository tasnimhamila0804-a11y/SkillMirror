import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/icon';

/** Public landing page: brand, product story and entry points. */
@Component({
  selector: 'sm-landing',
  imports: [RouterLink, IconComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent {
  protected readonly year = new Date().getFullYear();

  protected readonly steps = [
    {
      title: 'Profilage',
      description:
        'Un questionnaire guidé capture votre situation, vos compétences, vos niveaux et votre objectif professionnel.',
      icon: 'profile',
    },
    {
      title: 'Analyse',
      description:
        "Collez une offre d'emploi : l'IA extrait les compétences exigées, vous les vérifiez, puis le moteur calcule votre score d'adéquation.",
      icon: 'analysis',
    },
    {
      title: 'Projets',
      description:
        'Chaque écart prioritaire devient un projet pratique : étapes, tâches, critères d’évaluation et preuve attendue.',
      icon: 'learning',
    },
    {
      title: 'Preuves',
      description:
        'Vous soumettez dépôt, rapport ou lien : la compétence passe de « déclarée » à « démontrée », et votre score évolue.',
      icon: 'check',
    },
  ];

  protected readonly features = [
    {
      title: 'Miroir de compétences',
      description:
        'Un profil professionnel clair : compétences déclarées, évaluées et démontrées, expériences, projets et certifications.',
      icon: 'mirror',
    },
    {
      title: 'Career Gap Engine',
      description:
        "Un score de compatibilité pondéré 0–100, calculé côté backend, toujours accompagné de son explication.",
      icon: 'analysis',
    },
    {
      title: 'Project-to-Proof',
      description:
        'Des projets personnalisés à votre niveau et à votre rythme, qui se terminent par une preuve vérifiable.',
      icon: 'spark',
    },
    {
      title: 'Suivi de progression',
      description:
        'Historique des scores, évolution des compétences, timeline des réalisations : votre croissance est visible.',
      icon: 'progress',
    },
    {
      title: 'IA transparente',
      description:
        "Extraction et génération assistées par IA, avec résultats vérifiables — et mode démonstration sans clé API.",
      icon: 'info',
    },
    {
      title: 'Sécurisé',
      description:
        'Authentification JWT, mots de passe hachés, données isolées par utilisateur et contrôle d’accès côté serveur.',
      icon: 'admin',
    },
  ];

  protected readonly benefits = [
    {
      title: 'Vous ne devinez plus',
      description:
        "Plutôt que de postuler à l'aveugle, vous savez précisément ce qui vous manque pour le poste visé.",
    },
    {
      title: 'Vous apprenez avec un cap',
      description:
        'Chaque heure d’étude sert un écart identifié par une offre réelle, pas une liste générique de tutoriels.',
    },
    {
      title: 'Vous constituez des preuves',
      description:
        'Finies les compétences « déclarées sans preuve » : vos projets deviennent des réalisations consultables.',
    },
  ];
}
