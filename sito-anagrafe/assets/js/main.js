const canvas = document.getElementById("data-scene");
const ctx = canvas.getContext("2d");
const header = document.querySelector(".site-header");
const navToggle = document.querySelector(".nav-toggle");
const navLinks = document.querySelectorAll(".site-nav a");
const metrics = document.querySelectorAll(".metric");

let width = 0;
let height = 0;
let nodes = [];
let animationFrame = 0;

function resizeScene() {
    const ratio = window.devicePixelRatio || 1;
    width = window.innerWidth;
    height = window.innerHeight;
    canvas.width = Math.floor(width * ratio);
    canvas.height = Math.floor(height * ratio);
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
    createNodes();
}

function createNodes() {
    const total = width < 700 ? 34 : 64;
    nodes = Array.from({ length: total }, (_, index) => ({
        x: (index * 137) % width,
        y: (index * 89) % height,
        vx: ((index % 5) - 2) * 0.12,
        vy: ((index % 7) - 3) * 0.08,
        pulse: index * 0.37
    }));
}

function drawScene(time) {
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = "rgba(3, 7, 6, 0.42)";
    ctx.fillRect(0, 0, width, height);

    nodes.forEach((node) => {
        node.x += node.vx;
        node.y += node.vy;

        if (node.x < -20) node.x = width + 20;
        if (node.x > width + 20) node.x = -20;
        if (node.y < -20) node.y = height + 20;
        if (node.y > height + 20) node.y = -20;
    });

    for (let i = 0; i < nodes.length; i++) {
        for (let j = i + 1; j < nodes.length; j++) {
            const dx = nodes[i].x - nodes[j].x;
            const dy = nodes[i].y - nodes[j].y;
            const distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 150) {
                const opacity = 1 - distance / 150;
                ctx.strokeStyle = `rgba(99, 247, 220, ${opacity * 0.18})`;
                ctx.lineWidth = 1;
                ctx.beginPath();
                ctx.moveTo(nodes[i].x, nodes[i].y);
                ctx.lineTo(nodes[j].x, nodes[j].y);
                ctx.stroke();
            }
        }
    }

    nodes.forEach((node) => {
        const glow = 0.5 + Math.sin(time * 0.001 + node.pulse) * 0.5;
        ctx.fillStyle = `rgba(141, 255, 128, ${0.25 + glow * 0.35})`;
        ctx.fillRect(node.x - 2, node.y - 2, 4, 4);
        ctx.strokeStyle = "rgba(99, 247, 220, 0.36)";
        ctx.strokeRect(node.x - 6, node.y - 6, 12, 12);
    });

    ctx.strokeStyle = "rgba(255, 209, 102, 0.16)";
    ctx.lineWidth = 1;
    for (let y = height * 0.65; y < height; y += 28) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y + (height - y) * 0.15);
        ctx.stroke();
    }

    animationFrame = requestAnimationFrame(drawScene);
}

function animateMetrics() {
    metrics.forEach((metric) => {
        const target = Number(metric.dataset.count || "0");
        let value = 0;
        const step = Math.max(1, Math.ceil(target / 38));

        const tick = () => {
            value = Math.min(target, value + step);
            metric.textContent = target === 100 ? `${value}%` : String(value);

            if (value < target) {
                requestAnimationFrame(tick);
            }
        };

        tick();
    });
}

navToggle.addEventListener("click", () => {
    const isOpen = header.classList.toggle("is-open");
    navToggle.setAttribute("aria-expanded", String(isOpen));
});

navLinks.forEach((link) => {
    link.addEventListener("click", () => {
        header.classList.remove("is-open");
        navToggle.setAttribute("aria-expanded", "false");
    });
});

window.addEventListener("resize", resizeScene);
resizeScene();
animationFrame = requestAnimationFrame(drawScene);
animateMetrics();

window.addEventListener("pagehide", () => {
    cancelAnimationFrame(animationFrame);
});
