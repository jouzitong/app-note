import {
  mapNoteNodeDetailDtoToVm,
  mapNoteNodeDtoToVm,
  mapParentNodeToOptionVm,
  mapTagToOptionVm,
} from "@/mappers/domain/language-jp/note-node.mapper";

describe("note-node.mapper", () => {
  test("mapNoteNodeDtoToVm normalizes tags and fallback className", () => {
    const vm = mapNoteNodeDtoToVm({
      id: 1,
      title: "节点A",
      meta: {
        tags: [
          { id: 7, name: "N5" },
          { label: " 语法 ", className: "x" },
        ],
      },
    });

    expect(vm.id).toBe(1);
    expect(vm.title).toBe("节点A");
    expect(vm.meta.tags).toEqual([
      { id: 7, bizType: "NOTE", label: "N5", className: "app-tag--info" },
      { id: null, bizType: "NOTE", label: "语法", className: "x" },
    ]);
  });

  test("mapNoteNodeDetailDtoToVm maps child nodes and paths", () => {
    const vm = mapNoteNodeDetailDtoToVm({
      noteNode: { id: 2, title: "Root" },
      paths: [
        { id: 1, title: "P1" },
        { id: 2, title: "" },
      ],
      childNoteNodes: [
        {
          id: 3,
          title: "C1",
          sort: "9",
          noteType: "ARTICLE",
          meta: {
            tags: [{ id: 11, label: "语法", className: "app-tag--success" }],
          },
        },
      ],
      content: { k: "v" },
    });

    expect(vm.noteNode.id).toBe(2);
    expect(vm.paths).toEqual([{ id: 1, title: "P1" }]);
    expect(vm.childNodes).toEqual([
      {
        id: 3,
        title: "C1",
        sort: 9,
        noteType: "ARTICLE",
        tags: [
          {
            id: 11,
            bizType: "NOTE",
            label: "语法",
            className: "app-tag--success",
          },
        ],
      },
    ]);
    expect(vm.content).toEqual({ k: "v" });
  });

  test("mapParentNodeToOptionVm and mapTagToOptionVm validate input", () => {
    expect(mapParentNodeToOptionVm({ id: 8, title: "Parent" })).toEqual({
      id: 8,
      title: "Parent",
    });
    expect(mapParentNodeToOptionVm({ id: "x" })).toBeNull();

    expect(mapTagToOptionVm({ label: " Tag " })).toEqual({
      id: null,
      bizType: "NOTE",
      label: "Tag",
      className: "app-tag--info",
    });
    expect(mapTagToOptionVm({ label: "   " })).toBeNull();
  });
});
