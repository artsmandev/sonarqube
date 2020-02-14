CREATE TABLE "PERM_TPL_CHARACTERISTICS" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "TEMPLATE_ID" INTEGER NOT NULL,
  "PERMISSION_KEY" VARCHAR(64) NOT NULL,
  "WITH_PROJECT_CREATOR" BOOLEAN NOT NULL DEFAULT FALSE,
  "CREATED_AT" BIGINT NOT NULL,
  "UPDATED_AT" BIGINT NOT NULL
);
CREATE UNIQUE INDEX "UNIQ_PERM_TPL_CHARAC" ON "PERM_TPL_CHARACTERISTICS" ("TEMPLATE_ID", "PERMISSION_KEY");