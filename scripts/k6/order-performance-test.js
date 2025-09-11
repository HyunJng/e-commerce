import http from 'k6/http';
import { check, sleep, group } from 'k6';

// ======= CONFIG =======
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';
const BEST_API = `${BASE_URL}/api/v1/products/best`;
const ITEM_API = (id) => `${BASE_URL}/api/v1/products/${id}`;
const ORDER_API = `${BASE_URL}/api/v1/orders`;

const USE_COUPON = (__ENV.USE_COUPON || 'false').toLowerCase() === 'true';
const COUPON_ID_MIN = __ENV.COUPON_ID_MIN ? parseInt(__ENV.COUPON_ID_MIN) : 1;
const COUPON_ID_MAX = __ENV.COUPON_ID_MAX ? parseInt(__ENV.COUPON_ID_MAX) : 100;
const MAX_PRODUCT_ID_FALLBACK = __ENV.MAX_PRODUCT_ID ? parseInt(__ENV.MAX_PRODUCT_ID) : 10;

// ======= OPTIONS =======
export const options = {
    scenarios: {
        ramp_rps: {
            executor: 'ramping-arrival-rate',
            timeUnit: '1s',
            startRate: 50,          // 시작 50 RPS
            preAllocatedVUs: 300,   // 미리 띄울 VU
            maxVUs: 1200,           // RPS 유지에 필요한 여유치
            stages: [
                { target: 100, duration: '45s' },  // 45초 동안 100 RPS로 상승
                { target: 300, duration: '60s' },  // 1분 동안 300 RPS로 상승
                { target: 300, duration: '90s' },  // 1분 30초 유지
                { target: 0,   duration: '30s' },  // 30초 쿨다운
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.05'],
        'http_req_duration{group:Best Products}': ['p(95)<400'],
        'http_req_duration{group:Fetch Item}': ['p(95)<400'],
        'http_req_duration{group:Create Order}': ['p(95)<800'],
    },
    summaryTrendStats: ['min', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// ======= HELPERS =======
function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
function randomItem(arr) {
    return arr[randInt(0, arr.length - 1)];
}
function getRandomCouponId() {
    if (!USE_COUPON) return null;
    return randInt(COUPON_ID_MIN, COUPON_ID_MAX);
}

// ======= API CALLS =======
function fetchBestProducts() {
    const res = http.get(BEST_API, { tags: { name: 'best' } });
    check(res, { 'best: 200': (r) => r.status === 200 });
    if (res.status !== 200) return [];
    try {
        const list = res.json();
        return Array.isArray(list) ? list.map(p => p.id).filter(Boolean) : [];
    } catch { return []; }
}

function fetchItemDetail(productId) {
    const res = http.get(ITEM_API(productId), { tags: { name: 'item' } });
    check(res, { 'item: 200': (r) => r.status === 200 });
    if (res.status !== 200) return null;
    try {
        const body = res.json();
        return (body && body.id) ? body : null;
    } catch { return null; }
}

function createOrder(userId, couponId, orderItems) {
    const body = { userId, orderProduct: orderItems };
    if (couponId !== null) body.couponId = couponId;

    const res = http.post(ORDER_API, JSON.stringify(body), {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'order' },
    });
    check(res, { 'order: 200': (r) => r.status === 200 });
    return res;
}

// ======= TEST FLOW =======
let cachedBestIds = null;

export default function () {
    group('Load Test Scenario', function () {
        const userId = randInt(1, 10_000);

        // 1) 인기 상품 조회
        group('Best Products', function () {
            if (!cachedBestIds || cachedBestIds.length === 0 || Math.random() < 0.1) {
                cachedBestIds = fetchBestProducts();
            }
        });

        const candidates = (cachedBestIds && cachedBestIds.length > 0)
            ? cachedBestIds
            : Array.from({ length: 10 }, () => randInt(1, MAX_PRODUCT_ID_FALLBACK));

        // 2) 상품 상세 조회
        let chosenId = randomItem(candidates);
        let item = null;
        group('Fetch Item', function () {
            item = fetchItemDetail(chosenId);
        });
        if (!item) return;

        sleep(1);

        // 3) 주문
        group('Create Order', function () {
            const couponId = getRandomCouponId();
            const orderItems = [
                { productId: item.id, quantity: randInt(1, 3) }
            ];
            const res = createOrder(userId, couponId, orderItems);

            check(res, {
                'order: has orderId': (r) => {
                    try { const b = r.json(); return b && b.orderId; }
                    catch { return false; }
                },
            });
        });
    });
}