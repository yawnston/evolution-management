import type { Position } from "cytoscape";
import { ComparablePosition, PositionUpdateToServer } from "./Position";

export class SchemaObject {
    //key: number | undefined;
    //label: number | undefined;

    id!: number;
    label!: string;
    jsonValue!: string;
    position?: ComparablePosition;
    _originalPosition?: ComparablePosition;

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        object.label = JSON.parse(input.jsonValue).label;
        object.id = input.id;
        object.jsonValue = input.jsonValue;
        if (input.position) {
            object.position = new ComparablePosition(input.position);
            object._originalPosition = new ComparablePosition(input.position);
        }

        return object;
    }

    toPositionUpdateToServer(): PositionUpdateToServer | null {
        return this.position?.equals(this._originalPosition) ? null : new PositionUpdateToServer({ schemaObjectId: this.id, position: this.position });
    }
}

export class SchemaObjectFromServer {
    id!: number;
    jsonValue!: string;
    position?: Position;
}
