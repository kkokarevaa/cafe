-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: 127.0.0.1
-- Время создания: Фев 19 2025 г., 15:36
-- Версия сервера: 10.4.32-MariaDB-log
-- Версия PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `cafe`
--

-- --------------------------------------------------------

--
-- Структура таблицы `drinkingredients`
--

CREATE TABLE `drinkingredients` (
  `drink_id` int(11) NOT NULL,
  `ingredient_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `drinkingredients`
--

INSERT INTO `drinkingredients` (`drink_id`, `ingredient_id`) VALUES
(1, 2),
(2, 1),
(3, 3);

-- --------------------------------------------------------

--
-- Структура таблицы `drinks`
--

CREATE TABLE `drinks` (
  `drink_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `drinks`
--

INSERT INTO `drinks` (`drink_id`, `name`, `description`, `photo_url`, `price`) VALUES
(1, 'Эспрессо', 'Крепкий чёрный кофе', 'url_к_фото_эспрессо', 250.00),
(2, 'Латте', 'Эспрессо с паровым молоком', 'url_к_фото_латте', 300.00),
(3, 'Капучино', 'Эспрессо с вспененным молоком', 'url_к_фото_капучино', 350.00);

-- --------------------------------------------------------

--
-- Структура таблицы `ingredients`
--

CREATE TABLE `ingredients` (
  `ingredient_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `ingredients`
--

INSERT INTO `ingredients` (`ingredient_id`, `name`, `price`) VALUES
(1, 'Молоко', 50.00),
(2, 'Сахар', 20.00),
(3, 'Корица', 30.00);

-- --------------------------------------------------------

--
-- Структура таблицы `orderitems`
--

CREATE TABLE `orderitems` (
  `order_id` int(11) NOT NULL,
  `drink_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `orderitems`
--

INSERT INTO `orderitems` (`order_id`, `drink_id`, `quantity`) VALUES
(1, 1, 2),
(2, 2, 1),
(2, 3, 1);

-- --------------------------------------------------------

--
-- Структура таблицы `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `order_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `orders`
--

INSERT INTO `orders` (`order_id`, `user_id`, `order_date`, `total_price`) VALUES
(1, 1, '2025-02-19 14:34:52', 600.00),
(2, 2, '2025-02-19 14:34:52', 700.00);

-- --------------------------------------------------------

--
-- Структура таблицы `recipes`
--

CREATE TABLE `recipes` (
  `drink_id` int(11) NOT NULL,
  `recipe_text` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `recipes`
--

INSERT INTO `recipes` (`drink_id`, `recipe_text`) VALUES
(1, 'Сварить порцию эспрессо.'),
(2, 'Сварить порцию эспрессо и добавить паровое молоко.'),
(3, 'Сварить порцию эспрессо и добавить вспененное молоко.');

-- --------------------------------------------------------

--
-- Структура таблицы `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`) VALUES
(1, 'Клиент'),
(2, 'Сотрудник');

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role_id`) VALUES
(1, 'user', '1', 1),
(2, 'sotr', '1', 2);

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `drinkingredients`
--
ALTER TABLE `drinkingredients`
  ADD PRIMARY KEY (`drink_id`,`ingredient_id`),
  ADD KEY `ingredient_id` (`ingredient_id`);

--
-- Индексы таблицы `drinks`
--
ALTER TABLE `drinks`
  ADD PRIMARY KEY (`drink_id`);

--
-- Индексы таблицы `ingredients`
--
ALTER TABLE `ingredients`
  ADD PRIMARY KEY (`ingredient_id`);

--
-- Индексы таблицы `orderitems`
--
ALTER TABLE `orderitems`
  ADD PRIMARY KEY (`order_id`,`drink_id`),
  ADD KEY `drink_id` (`drink_id`);

--
-- Индексы таблицы `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Индексы таблицы `recipes`
--
ALTER TABLE `recipes`
  ADD PRIMARY KEY (`drink_id`);

--
-- Индексы таблицы `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `drinks`
--
ALTER TABLE `drinks`
  MODIFY `drink_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT для таблицы `ingredients`
--
ALTER TABLE `ingredients`
  MODIFY `ingredient_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT для таблицы `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT для таблицы `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT для таблицы `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `drinkingredients`
--
ALTER TABLE `drinkingredients`
  ADD CONSTRAINT `drinkingredients_ibfk_1` FOREIGN KEY (`drink_id`) REFERENCES `drinks` (`drink_id`),
  ADD CONSTRAINT `drinkingredients_ibfk_2` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`ingredient_id`);

--
-- Ограничения внешнего ключа таблицы `orderitems`
--
ALTER TABLE `orderitems`
  ADD CONSTRAINT `orderitems_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  ADD CONSTRAINT `orderitems_ibfk_2` FOREIGN KEY (`drink_id`) REFERENCES `drinks` (`drink_id`);

--
-- Ограничения внешнего ключа таблицы `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Ограничения внешнего ключа таблицы `recipes`
--
ALTER TABLE `recipes`
  ADD CONSTRAINT `recipes_ibfk_1` FOREIGN KEY (`drink_id`) REFERENCES `drinks` (`drink_id`);

--
-- Ограничения внешнего ключа таблицы `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
