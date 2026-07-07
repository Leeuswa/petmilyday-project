document.addEventListener("DOMContentLoaded", function () {
    const prefersReducedMotion = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    ).matches;

    if (prefersReducedMotion) {
        return;
    }

    const revealTargets = document.querySelectorAll(
        ".hero-main-card, .stats-strip, .svc, .review-card, .cta-section"
    );

    revealTargets.forEach(function (target) {
        target.classList.add("reveal-target");
    });

    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add("show");
            }
        });
    }, {
        threshold: 0.15
    });

    revealTargets.forEach(function (target) {
        observer.observe(target);
    });
});
