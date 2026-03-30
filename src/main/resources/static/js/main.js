/* ===================================================================
 * main.js - JavaScript cho Website Thương Mại Điện Tử
 * HoangDuongBinh - Đồ án giữa kỳ J2EE
 *
 * Chức năng:
 * 1. Toast notification (thông báo tự mất)
 * 2. Fade-in animation khi scroll
 * 3. Back-to-top button
 * 4. Smooth scroll
 * 5. Auto-hide alerts
 * =================================================================== */

document.addEventListener('DOMContentLoaded', function () {

    // =====================================================================
    //  1. AUTO-HIDE ALERTS - Tự ẩn thông báo sau 4 giây
    // =====================================================================
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        // Thêm animation fade-in
        alert.style.animation = 'slideDown 0.4s ease';

        // Tự ẩn sau 4 giây
        setTimeout(function () {
            alert.style.animation = 'fadeOut 0.5s ease forwards';
            setTimeout(function () {
                alert.remove();
            }, 500);
        }, 4000);
    });

    // =====================================================================
    //  2. FADE-IN ON SCROLL - Hiệu ứng xuất hiện khi cuộn trang
    // =====================================================================
    const fadeElements = document.querySelectorAll(
        '.product-card, .order-card, .admin-table-card, .admin-form-card, .cart-summary, .checkout-summary, .checkout-form-section, .order-detail-card'
    );

    // Thêm class để chuẩn bị animation
    fadeElements.forEach(function (el) {
        el.classList.add('fade-in-element');
    });

    // Intersection Observer: kích hoạt animation khi element vào viewport
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('fade-in-visible');
                    observer.unobserve(entry.target); // Chỉ animate 1 lần
                }
            });
        }, { threshold: 0.1 });

        fadeElements.forEach(function (el) {
            observer.observe(el);
        });
    } else {
        // Fallback cho trình duyệt cũ
        fadeElements.forEach(function (el) {
            el.classList.add('fade-in-visible');
        });
    }

    // =====================================================================
    //  3. BACK TO TOP BUTTON - Nút cuộn lên đầu trang
    // =====================================================================
    var backToTopBtn = document.createElement('button');
    backToTopBtn.innerHTML = '↑';
    backToTopBtn.className = 'back-to-top';
    backToTopBtn.title = 'Về đầu trang';
    document.body.appendChild(backToTopBtn);

    window.addEventListener('scroll', function () {
        if (window.pageYOffset > 300) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });

    backToTopBtn.addEventListener('click', function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    // =====================================================================
    //  4. CONFIRM DELETE - Xác nhận trước khi xóa (nâng cao UX)
    // =====================================================================
    var deleteLinks = document.querySelectorAll('a[onclick*="confirm"]');
    deleteLinks.forEach(function (link) {
        link.addEventListener('click', function () {
            // Thêm hiệu ứng nhấp nháy khi xóa
            var row = link.closest('tr');
            if (row) {
                row.style.transition = 'opacity 0.3s ease';
                row.style.opacity = '0.5';
                setTimeout(function () {
                    row.style.opacity = '1';
                }, 300);
            }
        });
    });

    // =====================================================================
    //  5. IMAGE PREVIEW - Preview ảnh khi nhập URL trong form sản phẩm
    // =====================================================================
    var imageUrlInput = document.getElementById('imageUrl');
    if (imageUrlInput) {
        var previewContainer = imageUrlInput.parentElement.querySelector('div');
        if (!previewContainer) {
            previewContainer = document.createElement('div');
            previewContainer.style.marginTop = '0.5rem';
            imageUrlInput.parentElement.appendChild(previewContainer);
        }

        imageUrlInput.addEventListener('input', function () {
            var url = imageUrlInput.value.trim();
            if (url && (url.startsWith('http://') || url.startsWith('https://'))) {
                previewContainer.innerHTML = '<img src="' + url + '" style="max-width:120px; border-radius:8px; border:2px solid #e2e8f0;" onerror="this.style.display=\'none\'">';
            } else {
                previewContainer.innerHTML = '';
            }
        });
    }

    // =====================================================================
    //  6. NAVBAR ACTIVE STATE - Đánh dấu trang hiện tại
    // =====================================================================
    var currentPath = window.location.pathname;
    var navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(function (link) {
        var href = link.getAttribute('href');
        if (href && currentPath === href) {
            link.classList.add('active');
        }
    });

    // =====================================================================
    //  7. QUANTITY INPUT ENHANCEMENT - Nút +/- cho số lượng giỏ hàng
    // =====================================================================
    var qtyInputs = document.querySelectorAll('.qty-input');
    qtyInputs.forEach(function (input) {
        input.addEventListener('change', function () {
            if (parseInt(input.value) < 1) input.value = 1;
            if (parseInt(input.value) > 99) input.value = 99;
        });
    });

    // =====================================================================
    //  8. AJAX ADD TO CART - Thêm vào giỏ hàng ko reload trang
    // =====================================================================
    var addToCartBtns = document.querySelectorAll('a[href^="/cart/add/"]');
    addToCartBtns.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var url = btn.getAttribute('href');

            fetch(url, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error('Network response was not ok.');
                })
                .then(data => {
                    if (data.status === 'success') {
                        showCartToast(data.message);
                    }
                })
                .catch(error => {
                    console.error('Error adding to cart:', error);
                    // Fallback bth
                    window.location.href = url;
                });
        });
    });

    function showCartToast(message) {
        // Tự tạo DOM cho Toast
        var toast = document.createElement('div');
        toast.className = 'toast-notification';
        toast.innerHTML = `
            <div class="toast-content">
                <i class="bi bi-cart-check-fill toast-icon"></i>
                <span>${message}</span>
            </div>
            <button class="toast-close" onclick="this.parentElement.remove()">
                <i class="bi bi-x"></i>
            </button>
        `;
        document.body.appendChild(toast);

        // Tự ẩn sau 3 giây
        setTimeout(function () {
            toast.classList.add('toast-hide');
            setTimeout(function () {
                toast.remove();
            }, 400);
        }, 3000);
    }
});
