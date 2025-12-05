// Toggle submenu
function toggleSubmenu(submenuId, trigger) {
    const submenu = document.getElementById(submenuId);
    const toggle = trigger.querySelector('.nav-toggle');

    submenu.classList.toggle('show');
    toggle.classList.toggle('expanded');
}

document.addEventListener('DOMContentLoaded', function () {
    const statNumbers = document.querySelectorAll('.stat-number');

    const animateNumber = (element, target) => {
        const duration = 1500;
        const start = 0;
        const increment = target / (duration / 16);
        let current = start;

        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = Math.floor(current).toLocaleString();
        }, 16);
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = parseInt(entry.target.getAttribute('data-target') || entry.target.textContent.replace(/,/g, ''));
                animateNumber(entry.target, target);
                observer.unobserve(entry.target);
            }
        });
    });

    statNumbers.forEach(stat => {
        const originalValue = stat.textContent.replace(/,/g, '');
        stat.setAttribute('data-target', originalValue);
        stat.textContent = '0';
        observer.observe(stat);
    });

    const activeLinks = document.querySelectorAll('.nav-link.active');
    activeLinks.forEach(link => {
        const submenu = link.parentElement.querySelector('.nav-submenu');
        if (submenu && !submenu.classList.contains('show')) {
            const trigger = link.parentElement.querySelector('.nav-link[onclick]');
            if (trigger) {
                const toggle = trigger.querySelector('.nav-toggle');
                submenu.classList.add('show');
                if (toggle) toggle.classList.add('expanded');
            }
        }
    });

    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-8px) scale(1.02)';
        });

        card.addEventListener('mouseleave', function () {
            this.style.transform = 'translateY(-4px) scale(1)';
        });
    });
});

document.addEventListener('keydown', function (e) {
    // Alt + D for Dashboard
    if (e.altKey && e.key === 'd') {
        e.preventDefault();
        window.location.href = '/dashboard';
    }

    // Alt + M for Members
    if (e.altKey && e.key === 'm') {
        e.preventDefault();
        window.location.href = '/admin/members';
    }

    // Alt + O for Orders
    if (e.altKey && e.key === 'o') {
        e.preventDefault();
        window.location.href = '/admin/orders/entire';
    }

    // Alt + A for AI Product Upload (NEW!)
    if (e.altKey && e.key === 'a') {
        e.preventDefault();
        window.location.href = '/admin/products/upload';
    }
});
