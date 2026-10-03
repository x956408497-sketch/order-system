# 绠€鏄撳湪绾胯璐т笅鍗曠郴缁?
褰撳墠杩涘害锛氱涓€鎵瑰晢鍝?鍒嗙被妯″潡宸插畬鎴愶紝鍙互鏈湴鍚姩鍚庣锛岀敤鎺ュ彛鏌ョ湅鍜屾祴璇曞晢鍝佹暟鎹€?
## 1. 鏈湴鍚姩

1. 纭鏈満宸插畨瑁?JDK 17銆丮aven銆丮ySQL銆?2. 纭 `src/main/resources/application.properties` 涓殑鏁版嵁搴撹处鍙峰瘑鐮佹纭€?3. 鍦?Navicat 鎴?MySQL 鍛戒护琛屾墽琛岋細

```sql
source src/main/resources/db/schema.sql
```

涔熷彲浠ョ洿鎺ユ妸璇ユ枃浠跺唴瀹瑰鍒跺埌 Navicat 鎵ц銆?
4. 鍚姩鍚庣锛?
```bash
./mvnw spring-boot:run
```

Windows 涓嬩篃鍙互鐩存帴鐢?IDEA 杩愯 `OrderSystemApplication`銆?
## 2. 绗竴鎵规帴鍙?
鐢ㄦ埛绔細

- `GET /hello`
- `GET /api/categories`
- `GET /api/products`
- `GET /api/products?categoryId=1`
- `GET /api/products?keyword=鑻规灉`
- `GET /api/products/{id}`

绠＄悊绔細

- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`
- `PUT /api/admin/products/{id}/status?status=1`
- `PUT /api/admin/products/{id}/stock?stock=50`

鏂板鍟嗗搧绀轰緥锛?
```json
{
  "categoryId": 1,
  "name": "姗欏瓙",
  "imageUrl": "",
  "price": 8.8,
  "stock": 30,
  "description": "娴嬭瘯鍟嗗搧",
  "status": 1
}
```

## 3. 涓嬩竴姝ュ缓璁?
1. 鍏堟妸鏈壒鎺ュ彛鐢ㄦ祻瑙堝櫒鎴?Postman 璺戦€氥€?2. 鍐嶅紑鍙戣喘鐗╄溅銆佹敹璐у湴鍧€銆?3. 鐒跺悗鍋氳鍗曞垱寤恒€佸簱瀛樻墸鍑忋€佹ā鎷熸敮浠樸€?4. 鏈€鍚庡啀鍋氬皬绋嬪簭椤甸潰鍜?Vue 鍟嗗鍚庡彴銆?
## 4. Spark 閿€鍞垎鏋愭ā鍧?
缁熻琛ㄨ剼鏈細

```text
src/main/resources/db/analytics.sql
```

鍏堝湪 MySQL/Navicat 涓墽琛岃鑴氭湰锛屽啀杩愯鐙珛椤圭洰锛?
```powershell
cd D:\姣曡\order-analysis
mvn clean compile
mvn exec:java
```

Spark 浼氳鍙?`orders`銆乣order_item`銆乣product`銆乣category`锛屽彧缁熻 `PAID`銆乣TO_SHIP`銆乣SHIPPED`銆乣COMPLETED` 璁㈠崟锛屽苟鍐欏叆锛?
- `sales_statistics`
- `product_sales_statistics`
- `category_sales_statistics`

Spring Boot 鏌ヨ鎺ュ彛锛?
- `GET /api/admin/statistics/overview`
- `GET /api/admin/statistics/sales-trend`
- `GET /api/admin/statistics/top-products`
- `GET /api/admin/statistics/categories`

