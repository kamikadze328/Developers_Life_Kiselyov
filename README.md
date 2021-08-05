# Developers Life
Completed test task for Tinkoff Summer School 2021
### Download:
- [Google play](https://play.google.com/store/apps/details?id=com.kamikadze328.developerslife)  
- [AppGalerry](https://appgallery.huawei.com/#/app/C104573145)  
  
## Android app (Kotlin)
![Developers life promoution image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/0_main_image.png)
![Mem 1 image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/1_mem_1.jpg)
![Mem 2 image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/2_mem_2.jpg)
![Mem 3 image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/3_mem_3.jpg)
![No internet state image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/4_no_internet.jpg)
![Internet problem state image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/5_internet_problem.jpg)
![Loading state image](https://github.com/kamikadze328/Developers_Life/blob/main/images/Screenshots/6_loading.jpg)


## Android Developer, Сириус 2021  
### Вам предлагается написать Android-приложение, обладающее следующим минимальным функционалом:  
1. На главном экране приложения отображается gif-изображение вместе с его описанием, полученным с сайта developerslife.ru (детали работы с API — ниже)*  
2. Под изображением располагаются две кнопки:  
  2.1. Одна (кнопка «следующий») загружает следующий рандомный пост (вызывая метод API).  
  2.2. Вторая — позволяет вернуться к предыдущему посту, который мы сохранили в кэш после загрузки.  
  2.3. Получается такая структура: (изначально кнопка «назад» не активна, но уже есть какой- то пост, загруженный при старте) нажимаем на «следующий» — появляется пост и кнопка «назад» становится активна — нажимаем на «следующий» ещё раз — появляется новый пост и обе кнопки активны — нажимаем «назад» и попадаем на один пост назад (обе кнопки активны) — нажимаем ещё раз и попадаем на первую картинку (кнопка «назад» стала не активна снова). Теперь если кликнуть на «следующий» мы сначала должны пройти те посты, которые у нас были загружены, а потом, как они кончатся — загружать новые.
3. Ответы от API должны быть закешированы хотя бы на время сессии для реализации переходов «назад».  
4. Должны быть предусмотрены различные состояния загрузки данных: ошибка загрузки, загрузка и успешная загрузка.  
  
### Будет здорово (но не обязательно), если вы позаботитесь о следующем:  
+ Приложение будет написано на языке Kotlin.  
+	Общая плавность и стабильность приложения.  
+	Возможность отображать gif-изображения из трех разных категорий сайта: горячее, последнее, популярное.  
  + GET http://developerslife.ru/<раздел>/<номер страницы>?json=true  
  + Виды разделов: о latest — последние о	hot — горячие 
    +	top — лучшие  
    + latest - последние  
    + hot - лучшие  
      + Пример: https://developerslife.ru/latest/0?ison=true  
+ UNIT-тесты
+ Анимации

#### *Примечания к реализации:  
+	Для работы с gif-изображениями рекомендуется использовать [Glide](https://github.com/bumptech/glide)
+	Для получения постов необходимо использовать следующий метод:  
  + Get https://developerslife.ru/random?json=true  
+	Для того, чтобы вам было легче справиться с заданием, ниже вы найдете примерные макеты приложения: 
