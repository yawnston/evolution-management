import { IntendedStringBuilder } from "@/utils/string";
import { Signature, StaticName, type StaticNameJSON } from "@/types/identifiers";
import type { ComplexPropertyJSON } from "./ComplexProperty";
import { subpathFromJSON, type ChildProperty } from "./compositeTypes";

export type RootPropertyJSON = ComplexPropertyJSON & { name: StaticNameJSON };

export class RootProperty {
    name: StaticName;
    _subpaths: ChildProperty[];
    _signature = Signature.null;

    constructor(name: StaticName, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
    }

    get isAuxiliary(): boolean {
        return true;
    }

    get signature(): Signature {
        return this._signature;
    }

    get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    static fromJSON(jsonObject: RootPropertyJSON): RootProperty {
        const property = new RootProperty(StaticName.fromJSON(jsonObject.name));

        property._subpaths = jsonObject.subpaths.map(subpath => subpathFromJSON(subpath, property));

        return property;
    }

    toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
    }

    toJSON(): ComplexPropertyJSON {
        return {
            _class: 'ComplexProperty',
            name: this.name.toJSON(),
            signature: this._signature.toJSON(),
            subpaths: this._subpaths.map(subpath => subpath.toJSON())
        };
    }
}
