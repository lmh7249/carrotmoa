document.addEventListener('DOMContentLoaded', function() {
    const carouselSlide = document.getElementById('carousel-slide');
    const prevButton = document.getElementById('carousel-prev');
    const nextButton = document.getElementById('carousel-next');

    // 모든 이미지 요소 가져오기
    const images = carouselSlide.getElementsByTagName('img');
    const slideWidth = images[0].clientWidth;

    // 현재 슬라이드 인덱스
    let counter = 0;

    // 이미지 위치 초기화
    carouselSlide.style.transform = 'translateX(0)';

    // 다음 버튼 클릭 이벤트
    nextButton.addEventListener('click', function() {
        if (counter >= images.length - 1) {
            counter = -1;
        }
        counter++;
        updateSlidePosition();
    });

    // 이전 버튼 클릭 이벤트
    prevButton.addEventListener('click', function() {
        if (counter <= 0) {
            counter = images.length;
        }
        counter--;
        updateSlidePosition();
    });

    // 슬라이드 위치 업데이트 함수
    function updateSlidePosition() {
        carouselSlide.style.transition = 'transform 0.4s ease-in-out';
        carouselSlide.style.transform = `translateX(${-slideWidth * counter}px)`;
    }

    // 자동 슬라이드 기능
    let autoSlideInterval = setInterval(function() {
        if (counter >= images.length - 1) {
            counter = -1;
        }
        counter++;
        updateSlidePosition();
    }, 5000); // 5초마다 슬라이드 변경

    // 마우스가 캐러셀 위에 있을 때 자동 슬라이드 중지
    const carouselContainer = document.getElementById('carousel-container');
    carouselContainer.addEventListener('mouseenter', function() {
        clearInterval(autoSlideInterval);
    });

    // 마우스가 캐러셀을 벗어났을 때 자동 슬라이드 재시작
    carouselContainer.addEventListener('mouseleave', function() {
        autoSlideInterval = setInterval(function() {
            if (counter >= images.length - 1) {
                counter = -1;
            }
            counter++;
            updateSlidePosition();
        }, 5000);
    });
});