CREATE TABLE "GROUPS"(
    "ID" INTEGER NOT NULL AUTO_INCREMENT (1,1),
    "UUID" VARCHAR(40) NOT NULL,
    "ORGANIZATION_UUID" VARCHAR(40) NOT NULL,
    "NAME" VARCHAR(500),
    "DESCRIPTION" VARCHAR(200),
    "CREATED_AT" TIMESTAMP,
    "UPDATED_AT" TIMESTAMP
);
ALTER TABLE "GROUPS" ADD CONSTRAINT "PK_GROUPS" PRIMARY KEY("ID");

CREATE TABLE "GROUP_ROLES"(
    "ORGANIZATION_UUID" VARCHAR(40) NOT NULL,
    "GROUP_ID" INTEGER,
    "GROUP_UUID" VARCHAR(40),
    "ROLE" VARCHAR(64) NOT NULL,
    "COMPONENT_UUID" VARCHAR(40),
    "UUID" VARCHAR(40) NOT NULL
);
ALTER TABLE "GROUP_ROLES" ADD CONSTRAINT "PK_GROUP_ROLES" PRIMARY KEY("UUID");
CREATE INDEX "GROUP_ROLES_COMPONENT_UUID" ON "GROUP_ROLES"("COMPONENT_UUID");
CREATE UNIQUE INDEX "UNIQ_GROUP_ROLES" ON "GROUP_ROLES"("ORGANIZATION_UUID", "GROUP_ID", "COMPONENT_UUID", "ROLE");