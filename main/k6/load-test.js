import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = "http://localhost"; // 서버 내부에서 실행 (nginx → app)

const errorRate = new Rate("error_rate");
const productListDuration = new Trend("product_list_duration");
const productDetailDuration = new Trend("product_detail_duration");

export const options = {
	stages: [
		{ duration: "30s", target: 50 }, // 30초 동안 50 VU로 증가
		{ duration: "1m", target: 50 }, // 1분 유지
		{ duration: "10s", target: 0 }, // 종료
	],
	thresholds: {
		http_req_duration: ["p(95)<500"], // p95 응답시간 500ms 이내
		error_rate: ["rate<0.01"], // 에러율 1% 미만
	},
};

export default function () {
	// 1. 상품 목록 (캐시 적용)
	const listRes = http.get(`${BASE_URL}/`);
	check(listRes, { "list 200": (r) => r.status === 200 });
	productListDuration.add(listRes.timings.duration);
	errorRate.add(listRes.status !== 200);
	sleep(0.5);

	// 2. 상품 상세 (캐시 적용)
	const productId = Math.floor(Math.random() * 1000) + 2031;
	const detailRes = http.get(`${BASE_URL}/products/${productId}`);
	check(detailRes, { "detail 200": (r) => r.status === 200 });
	productDetailDuration.add(detailRes.timings.duration);
	errorRate.add(detailRes.status !== 200);
	sleep(1);
}
