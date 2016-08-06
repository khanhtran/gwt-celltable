package com.sample.sort.client;

import com.sample.sort.shared.FieldVerifier;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Sort implements EntryPoint {
	// A simple data type that represents a contact.
	static Logger logger = Logger.getLogger("Sort");

	private static class Contact {
		private final String address;
		private final String name;
		private double mark;

		public Contact(String name, String address, double mark) {
			this.name = name;
			this.address = address;
			this.mark = mark;
		}
	}

	private static class MarkFooter extends Header<String> {
		public MarkFooter(Cell<String> cell) {
			super(cell);
		}

		private double totalMark;

		public void setTotalMark(double total) {
			this.totalMark = total;
		}

		@Override
		public String getValue() {
			return String.valueOf(totalMark);
		}

	}

	// The list of data to display.
	private static List<Contact> CONTACTS = Arrays.asList(new Contact("John", "123 Fourth Road", .4),
			new Contact("Mary", "222 Lancer Lane", .7), new Contact("Zander", "94 Road Street", .8));

	public void onModuleLoad() {
		final CellTable<Contact> table = new CellTable<Contact>();
		ListDataProvider<Contact> dataProvider = new ListDataProvider<Contact>();
		List<Contact> list = dataProvider.getList();
		for (Contact contact : CONTACTS) {
			list.add(contact);
		}
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);
		// Create a CellTable.

		// Create name column.
		TextColumn<Contact> nameColumn = new TextColumn<Contact>() {
			@Override
			public String getValue(Contact contact) {
				return contact.name;
			}
		};
		// Make the name column sortable.
		nameColumn.setSortable(true);
		ListHandler<Contact> nameColumnSortHandler = new ListHandler<Contact>(list);
		nameColumnSortHandler.setComparator(nameColumn, new Comparator<Contact>() {
			public int compare(Contact o1, Contact o2) {
				if (o1 == o2) {
					return 0;
				}
				// Compare the name columns.
				if (o1 != null) {
					return (o2 != null) ? o1.name.compareTo(o2.name) : 1;
				}
				return -1;
			}
		});
		table.addColumnSortHandler(nameColumnSortHandler);
		table.addColumn(nameColumn, "Name");

		// Create address column.
		TextColumn<Contact> addressColumn = new TextColumn<Contact>() {
			@Override
			public String getValue(Contact contact) {
				return contact.address;
			}
		};
		addressColumn.setSortable(true);
		ListHandler<Contact> addressColumnSortHandler = new ListHandler<Contact>(list);
		nameColumnSortHandler.setComparator(addressColumn, new Comparator<Contact>() {
			public int compare(Contact o1, Contact o2) {
				if (o1 == o2) {
					return 0;
				}

				// Compare the name columns.
				if (o1 != null) {
					return (o2 != null) ? o1.address.compareTo(o2.address) : 1;
				}
				return -1;
			}
		});
		table.addColumnSortHandler(addressColumnSortHandler);
		table.addColumn(addressColumn, "Address");
		////////////// MARK
		// Create mark Column with cell validation
		final ValidatableInputCell markCell = new ValidatableInputCell("Mark must be positive");
		Column<Contact, String> markColumn = new Column<Contact, String>(markCell) {
			@Override
			public String getValue(Contact contact) {
				return String.valueOf(contact.mark);
			}
		};

		markColumn.setCellStyleNames("number-cell");
		markColumn.setSortable(true);
		final MarkFooter mf = new MarkFooter(new TextCell());
		mf.setTotalMark(calculateSumMark());
		mf.setHeaderStyleNames("number-cell");
		table.insertColumn(0, markColumn, new TextHeader("Mark"), mf);

		markColumn.setFieldUpdater(new FieldUpdater<Contact, String>() {
			public void update(int index, final Contact object, final String value) {
				if (PositiveDouble.isValid(value)) {
					// The cell will clear the view data when it sees
					// the updated
					// value.
					object.mark = Double.parseDouble(value);
					double totalMark = calculateSumMark();
					mf.setTotalMark(totalMark);
					table.redraw();
					table.redrawFooters();
				} else {
					// Update the view data to mark the pending value as
					// invalid.
					ValidationData viewData = markCell.getViewData((object));
					viewData.setInvalid(true);

					// We only modified the cell, so do a local redraw.
					table.redraw();
				}
				// Perform validation after 2 seconds to simulate network delay.
				// new Timer() {
				// @Override
				// public void run() {
				//
				// }
				// }.schedule(1000);
			}
		});
		ListHandler<Contact> markColumnSortHandler = new ListHandler<Contact>(list);
		markColumnSortHandler.setComparator(markColumn, new Comparator<Contact>() {
			public int compare(Contact o1, Contact o2) {
				if (o1 == o2) {
					return 0;
				}

				// Compare the name columns.
				if (o1 != null) {
					if (o2 == null) {
						return 1;
					}
					if (o1.mark > o2.mark) {
						return 1;
					} else if (o1.mark < o2.mark) {
						return -1;
					} else {
						return 0;
					}
				}
				return -1;
			}
		});
		table.addColumnSortHandler(markColumnSortHandler);
		//
		// Add the columns.

		Column<Contact, Boolean> checkColumn = new Column<Contact, Boolean>(new CheckboxCell()) {
			@Override
			public Boolean getValue(Contact object) {
				return (object.mark > .5);
			}
		};
		checkColumn.setFieldUpdater(new FieldUpdater<Contact, Boolean>() {

			@Override
			public void update(int index, Contact object, Boolean value) {
				Column markColumn = table.getColumn(1);
				Cell cell = markColumn.getCell();

				if (value) {
					increaseMark();
				} else {

				}
				table.redraw();
			}
		});

		table.addColumn(checkColumn, "Check");
		// table.setWidth("80%", true);
		// table.setColumnWidth(markColumn, 10.0, Unit.PCT);

		// Clickable text cell column
		Column<Contact, String> clickColumn = new Column<Contact, String>(new ClickableTextCell() {
			@Override
			protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
				Contact c = (Contact) context.getKey();
				if (value != null && c.mark > .5) {
					sb.appendHtmlConstant("<span style='cursor:pointer;'>");
					sb.append(value);
					sb.appendHtmlConstant("</span>");
					
				}
			}
		}) {

			@Override
			public String getValue(Contact object) {
				return object.name;
			}
			
			@Override
			public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element elem, Contact c,
					NativeEvent event) {
				if (c.mark > 0.5) {
					super.onBrowserEvent(context, elem, c, event);
				}
			}
		};
		clickColumn.setFieldUpdater(new FieldUpdater<Contact, String>() {
			@Override
			public void update(int index, Contact object, String value) {
				// TODO Auto-generated method stub
				Window.alert("click link " + value + " " + index);
			}
		});
		clickColumn.setCellStyleNames("underline");
		table.addColumn(clickColumn);

		Column<Contact, String> btnColumn = new Column<Contact, String>(new ButtonCell() {
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				Contact contact = (Contact) context.getKey();
				if (contact.mark > 0.5) {
					super.render(context, value, sb);
				}
			}
		}) {
			@Override
			public String getValue(Contact object) {
				return object.name;
			}
		};

		btnColumn.setFieldUpdater(new FieldUpdater<Contact, String>() {
			@Override
			public void update(int index, Contact object, String value) {
				// TODO Auto-generated method stub
				Window.alert("click " + value + " " + index);
			}
		});
		table.addColumn(btnColumn);
		// DATA
		// Create a data provider.

		// We know that the data is sorted alphabetically by default.
		table.getColumnSortList().push(nameColumn);

		// Add it to the root panel.
		RootPanel.get().add(table);
	}

	public static void increaseMark() {
		for (int i = 0; i < CONTACTS.size(); i++) {
			Contact c = CONTACTS.get(i);
			c.mark = c.mark + .1;
		}
	}

	public static double calculateSumMark() {
		double sum = 0;
		for (int i = 0; i < CONTACTS.size(); i++) {
			Contact c = CONTACTS.get(i);
			sum += c.mark;
		}
		return sum;
	}

}
