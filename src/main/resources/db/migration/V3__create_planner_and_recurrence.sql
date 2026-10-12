-- V3: Weekly Plans, Planned Items and Recurrence Engine
CREATE TABLE IF NOT EXISTS weekly_plans (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    week_start_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_week (user_id, week_start_date),
    CONSTRAINT fk_weekly_plans_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recurring_rules (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    frequency VARCHAR(30) NOT NULL, -- DAILY, WEEKLY, BIWEEKLY, SELECTED_WEEKDAY
    target_day_of_week VARCHAR(20), -- MONDAY, TUESDAY, etc.
    quantity INT NOT NULL DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_recurring_user (user_id),
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS planned_items (
    id BINARY(16) NOT NULL,
    weekly_plan_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    plan_date DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    notes VARCHAR(255),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_rule_id BINARY(16),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_planned_items_plan_date (weekly_plan_id, plan_date),
    CONSTRAINT fk_planned_items_plan FOREIGN KEY (weekly_plan_id) REFERENCES weekly_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_planned_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_planned_items_recurrence FOREIGN KEY (recurring_rule_id) REFERENCES recurring_rules (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
